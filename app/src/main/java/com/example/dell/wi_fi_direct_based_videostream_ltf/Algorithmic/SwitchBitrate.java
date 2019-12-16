package com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic;

import java.util.concurrent.ArrayBlockingQueue;

public class SwitchBitrate {

    private static final int through_Queue_size=3;
    private static final int Queue_size=3;
    private double y=0.75;//EWMA中的加权值
    private ArrayBlockingQueue<Integer> EWMA_through=new ArrayBlockingQueue<>(through_Queue_size);
    private  double alpha=0.00006;
    private double w1=0;
    private double w2=0;
    private double beita=0;
    private ArrayBlockingQueue<Double> w1_queue=new ArrayBlockingQueue<>(Queue_size);
    private ArrayBlockingQueue<Double> w2_queue=new ArrayBlockingQueue<>(Queue_size);
    private ArrayBlockingQueue<Double> e_queue=new ArrayBlockingQueue<>(Queue_size);
    /**
     *
     * @param through_put 吞吐量测量值
     * @param i 循环轮次
     * @return
     */
    public int Throughput_p(ArrayBlockingQueue<Integer> through_put,long i){
        int ewma_last;
        int result;
        if (through_put.size()==0)
            return -1;
        int current=through_put.poll();//当前时刻的吞吐量测量值
        if (i<5){
            y=Math.min(y,(1+i)/10+i);
        }

        if (EWMA_through.size()!=0)
            ewma_last= EWMA_through.poll();
        else ewma_last=-1;

        result=(int) y*ewma_last+(int)(1-y)*current;
        if (EWMA_through.size()>=through_Queue_size){
            EWMA_through.offer(result);
        }
        EWMA_through.offer(result);
        return result;
    }

    /**
     *
     * @param RSSI_queue RSSI测量值
     * @return
     */
    public int RSSI_p(ArrayBlockingQueue<Integer>RSSI_queue){
        int result=-50;
        int current_rssi=-50;
        int last_rssi;//最近一时刻的RSSI
        int last_last_rssi;//最近第二个时刻的RSSI
        double w1_n_1=w1;
        double w2_n_1=w2;
        double e_n_1=0;
        int e;
        if(RSSI_queue.size()>=3){
            current_rssi=RSSI_queue.poll();
            last_last_rssi=RSSI_queue.poll();
            last_rssi=RSSI_queue.poll();
        }else if (RSSI_queue.size()==2){
            last_last_rssi=RSSI_queue.poll();
            last_rssi=last_last_rssi;
        }else {
            last_last_rssi=last_rssi=-1;
        }
        result=(int)(w1*last_rssi+w2*last_last_rssi+beita);
        e=current_rssi-result;
        if (w1_queue.size()>=2){
            w1_n_1=w1_queue.poll();
        }

        if (w2_queue.size()>=2){
            w2_n_1=w2_queue.poll();
        }

        w1=w1_n_1+2*alpha*e*last_rssi;
        w2=w2_n_1+2*alpha*e*last_last_rssi;

        w1_queue.offer(w1);
        w2_queue.offer(w2);
        if (e_queue.size()>1)
            e_n_1=e_queue.poll();
        e_queue.offer(e_n_1);

        beita=beita+alpha*e_n_1;
        return result;
    }
}
