package org.dync.queue;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 具体执行相关业务的业务类
 * @date 2019/1/11 13:49
 */
public class DelayOrderWorker implements Runnable {


    private String id;

    private String name;

    private String action;


    private Object obj;

    public DelayOrderWorker() {

    }

    public DelayOrderWorker(String id) {
        this.id = id;
    }


    public DelayOrderWorker(String id, Object obj) {
        this.id = id;
        this.obj = obj;

    }



    @Override
    public void run() {
        //相关业务逻辑处理
        System.out.println(Thread.currentThread().getName() + " do something action: " + this.getAction() + " id : " + this.getId());
       /* switch (this.getAction()) {

            default:
                System.out.println("处理的action [ {} ]"+ this.getAction());
                break;
        }
*/

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
