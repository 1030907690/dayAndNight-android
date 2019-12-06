package org.dync.queue;

import org.dync.utils.GlobalConfig;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 延时队列管理类，用来添加任务、执行任务
 * @date 2019/1/11 13:47
 */

@SuppressWarnings("AlibabaAvoidManuallyCreateThread")
public class DelayOrderQueueManager {

    /**
     * 默认 延时时间
     * 1800000
     * 180000L
     **/
    private Long defaultDelayTime = 1800000L;


    /**
     * 默认时间单位
     **/
    private final TimeUnit DEFAULT_SOURCE_UNIT = TimeUnit.MILLISECONDS;

    private final static int DEFAULT_THREAD_NUM = 5;
    // 固定大小线程池

//    private ExecutorService executor = GlobalConfig.getInstance().executorService();
    // 守护线程
    private Thread daemonThread;
    // 延时队列
    private DelayQueue<DelayOrderTask<DelayOrderWorker>> delayQueue = new DelayQueue<>();


    /***
     * 保存对象DelayOrderTask
     * */
    private Map<String, DelayOrderTask> delayOrderTaskMap = new ConcurrentHashMap<>();

    /**
     * 启动
     **/
    public DelayOrderQueueManager() {
        System.out.println("initializin DelayOrderQueueManager");
        //executor = GlobalConfig.getInstance().executorService();
        init();
    }


    public static DelayOrderQueueManager getInstance() {
        return null;
    }

    /**
     * 初始化
     */
    private void init() {
        //noinspection AlibabaAvoidManuallyCreateThread
        daemonThread = new Thread(() -> {
            execute();
        });
        daemonThread.setName("DelayQueueMonitor");
        daemonThread.start();
    }

    private void execute() {
        while (true) {
            Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
            //log.info("当前存活线程数量 [ {} ]" , map.size());
            int taskNum = delayQueue.size();
            //log.info("当前延时任务数量 [ {} ]" , taskNum);
            try {
                // 从延时队列中获取任务
                DelayOrderTask<DelayOrderWorker> delayOrderTask = delayQueue.take();
                if (delayOrderTask != null) {
                    DelayOrderWorker task = delayOrderTask.getTask();
                    if (null == task) {
                        System.out.println("task为null continue");
                        continue;
                    }
                    // 提交到线程池执行task
                    GlobalConfig.getInstance().executorService().execute(task);
                    //移除
                    delayOrderTaskMap.remove(task.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 添加任务
     *
     * @param task
     */
    public DelayOrderTask put(DelayOrderWorker task) {
        return put(task, defaultDelayTime, DEFAULT_SOURCE_UNIT);
    }


    /**
     * 添加任务
     *
     * @param task
     * @param time 延时时间
     * @param unit 时间单位
     */
    public DelayOrderTask put(DelayOrderWorker task, long time, TimeUnit unit) {
        // 获取延时时间
        long timeout = TimeUnit.NANOSECONDS.convert(time, unit);
        // 将任务封装成实现Delayed接口的消息体
        DelayOrderTask<DelayOrderWorker> delayOrder = new DelayOrderTask<>(timeout, task);
        // 将消息体放到延时队列中
        delayQueue.put(delayOrder);
        delayOrderTaskMap.put(task.getId(), delayOrder);
        System.out.println("新增task id [ {} ]" + task.getId());
        return delayOrder;
    }

    /**
     * 删除任务
     *
     * @param task
     * @return
     */
    public boolean removeTask(DelayOrderTask<DelayOrderWorker> task) {
        delayOrderTaskMap.remove(task.getTask().getId());
        boolean boo = delayQueue.remove(task);
        System.out.println("删除task removeTask id [ {} ] boo [ {} ] " + task.getTask().getId() + boo);
        return boo;
    }

    /**
     * 释放
     **/
    public boolean releaseTask(String id) {
        if (delayOrderTaskMap.containsKey(id)) {
            return this.removeTask(delayOrderTaskMap.get(id));
        }
        System.out.println("没有这个id [ {} ]" + id);
        return false;
    }



    /**
     * 判断是否存在
     **/
    public boolean containsKeyTask(String id) {
        return delayOrderTaskMap.containsKey(id);
    }

    /**得到task**/
    public DelayOrderTask getTask(String id) {
        return delayOrderTaskMap.get(id);
    }


    /***
     * zhouzhongqing
     *
     * 判断是否存在
     * */
    public boolean delayOrderTaskMapContainsKey(String id) {
        return delayOrderTaskMap.containsKey(id);
    }

    public Long getDefaultDelayTime() {
        return defaultDelayTime;
    }

    public void setDefaultDelayTime(Long defaultDelayTime) {
        this.defaultDelayTime = defaultDelayTime;
    }
}