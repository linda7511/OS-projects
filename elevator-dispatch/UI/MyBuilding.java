package UI;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import component.Elevator;
import component.EventListener;
import component.Floor;
import component.MyButton;


public class MyBuilding extends JFrame {
	//等待上行的楼层设置
	boolean whichFloorIsWaitUp[] = { false, false, false, false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false, false };

	//等待下行的楼层数组
	boolean whichFloorIsWaitDown[] = { false, false, false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false, false };

	Elevator[] elevators = new Elevator[5];//创建5个电梯类
    
	// 1-19层的上行按钮，舍弃数组第0个元素
	public static MyButton[] upButtons = new MyButton[20];
	
    // 2-20层的下行按钮，舍弃数组第0、1个元素
	public static MyButton[] downButtons = new MyButton[21];
	

	// main
	public static void main(String args[]) {
		MyBuilding frame = new MyBuilding();
		frame.setVisible(true);
	}

	//构造方法
	public MyBuilding() {
		setBackground(new Color(240, 248, 255));
		//设置窗口标题“操作系统——电梯调度算法”
		setTitle("电梯调度");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(135, 25, 950, 650);
		setLayout(null);

		// 显示各层上行按钮键（20层无上行键），为每层楼的上行键添加监听
		int x = 71, y = 600;
		for (int i = 1; i < 20; i++) {
			MyButton button = new MyButton("/image/up.png", "/image/upH.png", x, y - 30 * i, 30, 30);
			upButtons[i] = button;
			button.btn.addActionListener(new EventListener() {//监听上行键
				public void actionPerformed(ActionEvent e) {//某层楼上行键被触发
					button.btn.setEnabled(false);//该键禁用（再点击失效）
					whichFloorIsWaitUp[button.num+1] = true;
					System.out.println((button.num+1) + "楼等待上行");
					int choice = dispatchAlgorithm(button.num+1, 1);//经调度算法选出响应电梯
					elevators[choice].dest[button.num+1] = true;//将该楼层加入响应电梯目的地数组
				}
			});
			add(button.btn);
		}

		// 显示各层下行按钮键（1层无下行键），为每层楼的上行键添加监听
		x = 116;
		y = 600;
		for (int i = 2; i < 21; i++) {
			MyButton button = new MyButton("/image/down.png", "/image/downH.png", x, y - 30 * i, 30, 30);
			downButtons[i] = button;
			button.btn.addActionListener(new EventListener() {//监听所有下行键
				public void actionPerformed(ActionEvent e) {//某层楼下行键被触发
					button.btn.setEnabled(false);//该按键失效
					System.out.println(button.num);
					whichFloorIsWaitDown[button.num-17] = true;
					System.out.println((button.num-17) + "楼等待下行");
					int choice = dispatchAlgorithm(button.num-17, 0);//经调度算法选出响应电梯
					elevators[choice].dest[button.num-17] = true;//将该楼层加入响应电梯目的地数组
				}
			});
			add(button.btn);
		}

		//添加电梯

		Elevator elevator1 = new Elevator();
		elevator1.setLocation(150, 0);
		add(elevator1);
		elevators[0] = elevator1;

		Elevator elevator2 = new Elevator();
		elevator2.setLocation(300, 0);
		add(elevator2);
		elevators[1] = elevator2;

		Elevator elevator3 = new Elevator();
		elevator3.setLocation(450, 0);
		add(elevator3);
		elevators[2] = elevator3;

		Elevator elevator4 = new Elevator();
		elevator4.setLocation(600, 0);
		add(elevator4);
		elevators[3] = elevator4;

		Elevator elevator5 = new Elevator();
		elevator5.setLocation(750, 0);
		add(elevator5);
		elevators[4] = elevator5;

		// 添加楼层
		for (int i = 0; i < 20; i++) {
			add(new Floor().floor);
		}

		// 启动所有线程
		new Thread(elevator1).start();
		new Thread(elevator2).start();
		new Thread(elevator3).start();
		new Thread(elevator4).start();
		new Thread(elevator5).start();
	}

	// 电梯调度，callFloor-发出请求的楼层，up-请求的方向（1为上行，0为下行）
	public int dispatchAlgorithm(int callFloor, int up) {
		int elevatorNum = 0;
		int wayCost[] = { 0, 0, 0, 0, 0 };//该楼层对5部电梯的预计等待用时
		System.out.println("callfloor："+(callFloor));
		for (int j = 0; j < 5; j++) {
			int currentY = elevators[j].getMyY();
			if (elevators[j].alarm) {
				wayCost[j] += 1000000;
			} else {
					elevators[j].getFarthest();//计算当前任务用时
					wayCost[j] += elevators[j].timeTask;//该电梯完成当前任务所需用时
					System.out.println((j) + "电梯currenty："+(currentY));
					System.out.println((j) + "电梯waycost0："+(wayCost[j]));
					wayCost[j] += Math.abs(currentY - (600 - 30 * callFloor))*5;//加上电梯完成任务后赶来接应楼层的移动用时
					System.out.println((j) + "电梯waycost："+(wayCost[j]));
				/*if ((up == 1) && (this.whichFloorIsWaitDown[callFloor]) && (elevators[j].dest[callFloor - 1])) {
					//当前楼层将有
					wayCost[j] += 2000;
				}else if ((up == 0) && (this.whichFloorIsWaitUp[callFloor]) && (elevators[j].dest[callFloor - 1])) {
					wayCost[j] += 2000;
				}*/
				//选wayCost最小的电梯作为调度结果
				if (wayCost[j] < wayCost[elevatorNum])
					elevatorNum = j;
			}
		}
		return elevatorNum;
	}
}
