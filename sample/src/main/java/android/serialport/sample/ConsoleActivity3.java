/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.serialport.sample;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
//import android.serialport.sample.cmd;
import android.widget.TextView;
import android.widget.Button;
import android.app.Activity;
import android.view.View;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.TextView.OnEditorActionListener;
import java.util.Arrays;
import android.widget.Toast;
public class  ConsoleActivity3 extends SerialPortActivity {

    EditText mReception;
    TextView mReception_data;
    Button myButton2;
    Button myButton3;
    Button myButton4;
    Button myButton5;
    Button myButton6;
    Button myButton7;
    Button myButton8;
    private Integer mj = new Integer(0);
    private int [] ctdbyte = new int[128];
    byte [] ctdapdu = new byte[128];
    String ctdstrArray = "";
    private boolean mByteReceivedBack;
    String [] strtmp = new String [128];
    int i = 0;int  num=0;
    String  str;
    Object mByteReceivedBackSemaphore = new Object();
    byte[] mBufferbuffer_feijie_reset =    { (byte)0x1,(byte)0x2,(byte)0x0,(byte)0x02,(byte)0xd1,(byte)0x0,(byte)0xd1,(byte)0x03};//非接触复位
    byte[] mBufferbuffer_feijie_poweroff = { (byte)0x1,(byte)0x2,(byte)0x0,(byte)0x02,(byte)0xd1,(byte)0x2,(byte)0xd3,(byte)0x03};//非接触下电
    byte[] mBufferbuffer_jiechu_reset =    { (byte)0x1,(byte)0x2,(byte)0x0,(byte)0x03,(byte)0xd2,(byte)0x01,(byte)0x02,(byte)0xd1,(byte)0x03};//接触复位，选择的是卡座2
    byte[] mBufferbuffer_jiechu_poweroff = { (byte)0x1,(byte)0x2,(byte)0x0,(byte)0x03,(byte)0xd2,(byte)0x03,(byte)0x02,(byte)0xd3,(byte)0x03};//接触下电，选择的是卡座2
    SendingThread mSendingctd;

    private class SendingThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(mBufferbuffer_feijie_reset);
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console3);

        //mSendingctd = new SendingThread();
        mj=0;
        /*********************************************非接触复位*******************************/
        myButton2 = (Button) findViewById(R.id.bt2);
        myButton2.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             try {
                                                 if (mOutputStream != null) {
                                                     mOutputStream.write(mBufferbuffer_feijie_reset);
                                                     //mReception.append(new String(buffer, 0, size));
                                                 } else {
                                                     return;
                                                 }
                                             } catch (IOException e) {
                                                 e.printStackTrace();
                                                 return;
                                             }

                                             synchronized (mByteReceivedBackSemaphore) {

                                                 try {
                                                     mByteReceivedBackSemaphore.wait(800);
                                                 } catch (InterruptedException e) {
                                                 }

                                                 if (mByteReceivedBack == true) {
                                                     runOnUiThread(new Runnable() {
                                                         public void run() {
                                                     mReception_data.setText("接收到的字节是\n" + ctdstrArray);
                                                         }
                                                     });
                                                     ctdstrArray = "";
                                                     mj=0;
                                                     mByteReceivedBack = false;
                                                 }   //if

                                             }   //synchronized
                                         }
                                     }
        );
/*********************************************非接触复位*******************************/

/*********************************************非接触apdu*******************************/
        myButton3 = (Button) findViewById(R.id.bt3);
        myButton3.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             mReception = (EditText) findViewById(R.id.EditTextReception);
                                             str=mReception.getText().toString();

                                             if(!str.isEmpty()) {
                                                 String[] strArray = str.split(",");
                                                 int[] num2 = new int[strArray.length];
                                                 byte [] ctdapdu_send = new byte[strArray.length+8];
                                                 for (int i = 0; i < strArray.length; i++) {
                                                     num2[i] = Integer.parseInt(strArray[i],16);
                                                     ctdapdu[i]=(byte) num2[i];
                                                     ctdapdu_send[6+i]=ctdapdu[i];
                                                 }
                                                 ctdapdu_send[0]=(byte)0x01;
                                                 ctdapdu_send[1]=(byte)0x02;
                                                 ctdapdu_send[2]=(byte)0x0;
                                                 ctdapdu_send[3]=(byte) (strArray.length+2);
                                                 ctdapdu_send[4]=(byte) (0xd1);
                                                 ctdapdu_send[5]=(byte) (0x01);
                                                 ctdapdu_send[5+strArray.length+1]=(byte) (0x04);//异或临时固定值
                                                 ctdapdu_send[5+strArray.length+1+1]=(byte) (0x03);


                                                 byte  temp=0;
                                                 for (int i = 0; i <(ctdapdu_send.length-6); i++) {
                                                     temp ^=ctdapdu_send[4+i];
                                                     System.out.printf("    temp=%x ,ctdapdu_send[%d]=%x    \n ",temp,4+i,ctdapdu_send[4+i]);
                                                 }
                                                 ctdapdu_send[5+strArray.length+1]=temp;

                                                 for (int i = 0; i < ctdapdu_send.length; i++) {
                                                     //System.out.printf(ctdapdu_send[i] + " " + "\n");
                                                     System.out.printf("ctdapdu_send[%d]=%x,      ctdapdu_send.length=%d \n ",i,ctdapdu_send[i],ctdapdu_send.length);
                                                 }
                                                 //System.out.printf("###################  输出整形数组   ################### \n");
                                                 //for (int i = 0; i < num2.length; i++) {
                                                 //    System.out.printf(num2[i] + " " + "\n");
                                                 //}

                                                 try {
                                                     if (mOutputStream != null) {
                                                         mOutputStream.write(ctdapdu_send);
                                                         //mReception.append(new String(buffer, 0, size));
                                                     } else {
                                                         return;
                                                     }
                                                 } catch (IOException e) {
                                                     e.printStackTrace();
                                                     return;
                                                 }


                                                 synchronized (mByteReceivedBackSemaphore) {

                                                     try {
                                                         mByteReceivedBackSemaphore.wait(2800);
                                                     } catch (InterruptedException e) {
                                                     }
                                                     System.out.printf("非接触apdu mByteReceivedBack=%b \n",mByteReceivedBack);
                                                     if (mByteReceivedBack == true) {
                                                         runOnUiThread(new Runnable() {
                                                             public void run() {
                                                                 mReception_data.setText("接收到的字节是\n" + ctdstrArray);
                                                             }
                                                         });
                                                         ctdstrArray = "";
                                                         mj=0;
                                                         mByteReceivedBack = false;
                                                     }   //if

                                                 }   //synchronized

                                             }

                                         }
                                     }
        );
/*********************************************非接触apdu*******************************/

/*********************************************非接触下电*******************************/
        myButton4 = (Button) findViewById(R.id.bt4);
        myButton4.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             try {
                                                 if (mOutputStream != null) {
                                                     mOutputStream.write(mBufferbuffer_feijie_poweroff);
                                                     //mReception.append(new String(buffer, 0, size));
                                                 } else {
                                                     return;
                                                 }
                                             } catch (IOException e) {
                                                 e.printStackTrace();
                                                 return;
                                             }

                                             synchronized (mByteReceivedBackSemaphore) {

                                                 try {
                                                     mByteReceivedBackSemaphore.wait(800);
                                                 } catch (InterruptedException e) {
                                                 }

                                                 if (mByteReceivedBack == true) {
                                                     runOnUiThread(new Runnable() {
                                                         public void run() {
                                                             mReception_data.setText("接收到的字节是\n" + ctdstrArray);
                                                         }
                                                     });
                                                     ctdstrArray = "";
                                                     mByteReceivedBack = false;
                                                 }   //if

                                             }   //synchronized


                                         }
                                     }
        );
/*********************************************非接触下电*******************************/

/*********************************************接触复位*******************************/
        myButton5 = (Button) findViewById(R.id.bt5);
        myButton5.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             try {
                                                 if (mOutputStream != null) {
                                                     mOutputStream.write(mBufferbuffer_jiechu_reset);
                                                     //mReception.append(new String(buffer, 0, size));
                                                 } else {
                                                     return;
                                                 }
                                             } catch (IOException e) {
                                                 e.printStackTrace();
                                                 return;
                                             }

                                             synchronized (mByteReceivedBackSemaphore) {

                                                 try {
                                                     mByteReceivedBackSemaphore.wait(800);
                                                 } catch (InterruptedException e) {
                                                 }

                                                 if (mByteReceivedBack == true) {
                                                     runOnUiThread(new Runnable() {
                                                         public void run() {
                                                             mReception_data.setText("接收到的字节是\n" + ctdstrArray);
                                                         }
                                                     });
                                                     ctdstrArray = "";
                                                     mj=0;
                                                     mByteReceivedBack = false;
                                                 }   //if

                                             }   //synchronized
                                         }
                                     }
        );
/*********************************************接触复位*******************************/

/*********************************************接触选卡座*******************************/
        myButton6 = (Button) findViewById(R.id.bt6);
        myButton6.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             mReception = (EditText) findViewById(R.id.EditTextReception);
                                             str=mReception.getText().toString();
                                             int num3;
                                             if(!str.isEmpty()) {
                                                 //String[] strArray = str.split(",");
                                                 //int[] num2 = new int[strArray.length];
                                                 byte [] ctdapdu_send = new byte[9];
                                                 //for (int i = 0; i < strArray.length; i++) {
                                                     num3 = Integer.parseInt(str,16);
                                                   //  ctdapdu[i]=(byte) num2[i];
                                                     ctdapdu_send[6]=(byte)num3;
                                                 System.out.printf("    ctdapdu_send[6]=%s    \n ",str);
                                                 System.out.printf("    ctdapdu_send[6]=%x    \n ",ctdapdu_send[6]);
                                                 //}
                                                 ctdapdu_send[0]=(byte)0x01;
                                                 ctdapdu_send[1]=(byte)0x02;
                                                 ctdapdu_send[2]=(byte)0x0;
                                                 ctdapdu_send[3]=(byte) (0x3);
                                                 ctdapdu_send[4]=(byte) (0xd2);
                                                 ctdapdu_send[5]=(byte) (0x00);
                                                 //ctdapdu_send[6]=(byte) (0x00);
                                                 ctdapdu_send[7]=(byte) (0x04);//异或临时固定值
                                                 ctdapdu_send[8]=(byte) (0x03);


                                                 byte  temp=0;
                                                 for (int i = 4; i <7; i++) {
                                                     temp ^=ctdapdu_send[i];
                                                     //System.out.printf("    temp=%x ,ctdapdu_send[%d]=%x    \n ",temp,4+i,ctdapdu_send[4+i]);
                                                 }
                                                 ctdapdu_send[7]=temp;

                                                 for (int i = 0; i < ctdapdu_send.length; i++) {
                                                     //System.out.printf(ctdapdu_send[i] + " " + "\n");
                                                     System.out.printf("ctdapdu_send[%d]=%x,      ctdapdu_send.length=%d \n ",i,ctdapdu_send[i],ctdapdu_send.length);
                                                 }
                                                 //System.out.printf("###################  输出整形数组   ################### \n");
                                                 //for (int i = 0; i < num2.length; i++) {
                                                 //    System.out.printf(num2[i] + " " + "\n");
                                                 //}

                                                 try {
                                                     if (mOutputStream != null) {
                                                         mOutputStream.write(ctdapdu_send);
                                                         //mReception.append(new String(buffer, 0, size));
                                                     } else {
                                                         return;
                                                     }
                                                 } catch (IOException e) {
                                                     e.printStackTrace();
                                                     return;
                                                 }


                                                 synchronized (mByteReceivedBackSemaphore) {

                                                     try {
                                                         mByteReceivedBackSemaphore.wait(800);
                                                     } catch (InterruptedException e) {
                                                     }

                                                     if (mByteReceivedBack == true) {
                                                         runOnUiThread(new Runnable() {
                                                             public void run() {
                                                                 mReception_data.setText("接收到的字节是\n" + ctdstrArray);
                                                             }
                                                         });
                                                         ctdstrArray = "";
                                                         mByteReceivedBack = false;
                                                     }   //if

                                                 }   //synchronized

                                             }

                                         }
                                     }
        );
/*********************************************接触选卡座*******************************/

/*********************************************接触apdu*******************************/
        myButton7 = (Button) findViewById(R.id.bt7);
        myButton7.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             mReception = (EditText) findViewById(R.id.EditTextReception);
                                             str=mReception.getText().toString();

                                             if(!str.isEmpty()) {
                                                 String[] strArray = str.split(",");
                                                 int[] num_jiechu = new int[strArray.length];
                                                 byte [] ctdapdu_send = new byte[strArray.length+9];
                                                 for (int i = 0; i < strArray.length; i++) {
                                                     num_jiechu[i] = Integer.parseInt(strArray[i],16);
                                                     ctdapdu[i]=(byte) num_jiechu[i];
                                                     ctdapdu_send[7+i]=ctdapdu[i];
                                                 }
                                                 ctdapdu_send[0]=(byte)0x01;
                                                 ctdapdu_send[1]=(byte)0x02;
                                                 ctdapdu_send[2]=(byte)0x0;
                                                 ctdapdu_send[3]=(byte) (strArray.length+3);
                                                 ctdapdu_send[4]=(byte) (0xd2);
                                                 ctdapdu_send[5]=(byte) (0x02);
                                                 ctdapdu_send[6]=(byte) (0x02);//暂时选定卡座2
                                                 ctdapdu_send[6+strArray.length+1]=(byte) (0x04);//异或临时固定值
                                                 ctdapdu_send[6+strArray.length+1+1]=(byte) (0x03);


                                                 byte  temp=0;
                                                 for (int i = 0; i <(ctdapdu_send.length-6); i++) {
                                                     temp ^=ctdapdu_send[4+i];
                                                     System.out.printf("    temp=%x ,ctdapdu_send[%d]=%x    \n ",temp,4+i,ctdapdu_send[4+i]);
                                                 }
                                                 ctdapdu_send[6+strArray.length+1]=temp;

                                                 for (int i = 0; i < ctdapdu_send.length; i++) {
                                                     //System.out.printf(ctdapdu_send[i] + " " + "\n");
                                                     System.out.printf("ctdapdu_send[%d]=%x,      ctdapdu_send.length=%d \n ",i,ctdapdu_send[i],ctdapdu_send.length);
                                                 }
                                                 //System.out.printf("###################  输出整形数组   ################### \n");
                                                 //for (int i = 0; i < num2.length; i++) {
                                                 //    System.out.printf(num2[i] + " " + "\n");
                                                 //}

                                                 try {
                                                     if (mOutputStream != null) {
                                                         mOutputStream.write(ctdapdu_send);
                                                         //mReception.append(new String(buffer, 0, size));
                                                     } else {
                                                         return;
                                                     }
                                                 } catch (IOException e) {
                                                     e.printStackTrace();
                                                     return;
                                                 }

                                                 //System.out.printf("    接触apdu  test1  \n ");
                                                 synchronized (mByteReceivedBackSemaphore) {
                                                   //  System.out.printf("    接触apdu  test2  \n ");
                                                     try {
                                                         mByteReceivedBackSemaphore.wait(2800);
                                                     } catch (InterruptedException e) {
                                                     }
                                                     System.out.printf("接触apdu mByteReceivedBack=%b \n",mByteReceivedBack);
                                                     if (mByteReceivedBack == true) {
                                                         System.out.printf("    接触apdu  test3  \n ");
                                                         runOnUiThread(new Runnable() {
                                                             public void run() {
                                                                 mReception_data.setText("接收到的字节是\n" + ctdstrArray);
                                                             }
                                                         });
                                                         ctdstrArray = "";
                                                         mByteReceivedBack = false;
                                                     }   //if

                                                 }   //synchronized

                                             }

                                         }
                                     }
        );
/*********************************************接触apdu*******************************/


/*********************************************接触下电*******************************/
        myButton8 = (Button) findViewById(R.id.bt8);
        myButton8.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             try {
                                                 if (mOutputStream != null) {
                                                     mOutputStream.write(mBufferbuffer_jiechu_reset);
                                                     //mReception.append(new String(buffer, 0, size));
                                                 } else {
                                                     return;
                                                 }
                                             } catch (IOException e) {
                                                 e.printStackTrace();
                                                 return;
                                             }

                                             synchronized (mByteReceivedBackSemaphore) {

                                                 try {
                                                     mByteReceivedBackSemaphore.wait(800);
                                                 } catch (InterruptedException e) {
                                                 }

                                                 if (mByteReceivedBack == true) {
                                                     runOnUiThread(new Runnable() {
                                                         public void run() {
                                                             mReception_data.setText("接收到的字节是\n" + ctdstrArray);
                                                         }
                                                     });
                                                     ctdstrArray = "";
                                                     mByteReceivedBack = false;
                                                 }   //if

                                             }   //synchronized
                                         }
                                     }
        );
/*********************************************接触下电*******************************/



        mReception_data = (TextView) findViewById(R.id.mReception2);
        //finish();

    }


    @Override
    protected  void onDataReceived(final byte[] buffer, final int size) {
        System.out.printf("  >>>>>>>>>>>接受数据    \n");
        synchronized (mByteReceivedBackSemaphore) {

                    if (mReception_data != null) {

                        for (i = 0; i < size; i++) {
                            ctdbyte[mj] = buffer[i];
                            //System.out.printf(" buffer[%d]=0x%x     \n", i, buffer[i]);
                            ctdbyte[mj]=ctdbyte[mj]& 0xff; //byte转为int

                            num=Integer.toString(ctdbyte[mj]).length();
                            if (num==3)
                                strtmp[mj]="  0x"+Integer.toHexString(ctdbyte[mj]);
                            else
                                strtmp[mj]="  0x0"+Integer.toHexString(ctdbyte[mj]);

                            ctdstrArray+=strtmp[mj];

                            System.out.printf("  buffer[%d]=0x%x  ctdbyte[%d]=0x%x  ctdbyte[3]=0x%x  mj=%d \n", i, buffer[i], mj,ctdbyte[mj],ctdbyte[3]+6,mj);
                            mj++;
                            if(mj==(ctdbyte[3]+6))
                            {
                                mj=0;
                                mByteReceivedBack = true;
                                mByteReceivedBackSemaphore.notify();
                            }

                        }

                    }

        }
    }

    @Override
    protected void onDestroy() {
        //if (mSendingThread != null) mSendingThread.interrupt();
        super.onDestroy();

        System.out.printf(" ############  activity1 被销毁了  #############     \n");
        finish();
    }





}
