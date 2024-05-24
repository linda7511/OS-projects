package component;

import java.awt.Font;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import UI.MyBuilding;

public class Elevator extends JPanel implements Runnable {
	// 电梯内的按钮
	Buttons buttons;

	// 电梯门开、关
	final public Image doorL;
	final public Image doorR;

	// 电梯号
	public JLabel label;

	// 电梯状态
	public boolean IsOpen;// 电梯门开（1）关（0）状态
	public int Orientation;//电梯运行方向，上（1）/下（-1）/静止（0）
	public boolean dest[] = { false, false, false, false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,false };// 目的楼层数组
	public boolean alarm;//警铃键状态（1为触发时）
	//图形界面电梯门的坐标
	private int x = 20;
	private int y = 570;//初始位于1楼
	// 电梯号
	static int cnt = 0;
	//完成当前任务所需时间
	public int timeTask = 0;

	//构造方法
	public Elevator() {
		cnt++;
		this.setLayout(null);
		this.setSize(150, 650);
		this.setOpaque(false);
		this.IsOpen = false;
		this.Orientation = 0;
		this.alarm = false;
		// this.setBackground(new Color(153, 204, 255));

		// 添加门的图标
		doorL = new ImageIcon(this.getClass().getResource("/image/door.png")).getImage();
		doorR = new ImageIcon(this.getClass().getResource("/image/door.png")).getImage();

		// 添加标签
		Font font = new Font("宋体", Font.PLAIN, 20);
		label = new JLabel(cnt + "号电梯");
		label.setFont(font);
		label.setSize(100, 20);
		label.setLocation(68, 35);
		add(label);

		// 添加按钮
		buttons = new Buttons();
		add(buttons);
		buttons.setLocation(65, 60);

		// 添加监听事件
		for (int i = 1; i <= 20; i++) {
			MyButton btn = buttons.buttons[i];
			btn.btn.addActionListener(new EventListener() {
				public void actionPerformed(ActionEvent e) {//触发楼层按键
					btn.btn.setEnabled(false);//按键禁用
					dest[btn.num+1] = true;//选中按钮的楼层添加到该电梯的dest数组
					System.out.println((btn.num) + "楼电梯内按键点下");
				}
			});
		}

		buttons.btnAlarm.btn.addActionListener(new EventListener() {
			public void actionPerformed(ActionEvent e) {//触发报警键
				alarm = true;
				buttons.setFloor(-1,0);
				buttons.btnAlarm.btn.setEnabled(false);//按键禁用
				alarming();
			}
		});

		buttons.btnOpen.btn.addActionListener(new EventListener() {
			public void actionPerformed(ActionEvent e) {//触发开门键
				buttons.btnOpen.btn.setEnabled(false);//按键禁用
				openDoor();//开门
			}
		});

		buttons.btnClose.btn.addActionListener(new EventListener() {
			public void actionPerformed(ActionEvent e) {//触发关门键
				buttons.btnClose.btn.setEnabled(false);//按键禁用
				closeDoor();//关门
			}
		});
	}

	// 重写JPanel的paint函数
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(doorR, x, y, 13, 30, null);
		g.drawImage(doorL, 33 + (20 - x), y, 13, 30, null);
	}
	// 重写线程的run函数
	public void run() {
		while (true) {
			if (alarm) {
				alarming();
				break;
			}
			dispatch();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//电梯移动到目的地中的floor楼层
	public void moveToFloor(int floor) {
		/*电梯恰好位于该楼层*/
		if(y==600-floor*30) {
			if(floor>1) {
				if(!MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()) {
					//电梯所在楼层下行键被按下
					MyBuilding.downButtons[20 - (y / 30)].btn.setEnabled(true);//恢复该楼层下行键
				}else {
					MyBuilding.upButtons[20 - (y / 30)].btn.setEnabled(true);//恢复该楼层上行键
				}
			}
			dest[20 - (y / 30 )] = false;//该楼层移出目的地数组
			return;
		}
		/*电梯位于目标楼层下方*/
		while (y > 600 - floor * 30) {
			Orientation = 1;//电梯置于上行状态
			if (alarm) {
				return;
			}
			y -= 1;//电梯上移一个单位距离
			this.repaint();//图形界面更新显示
			if (y % 30 == 0) {//电梯移动到楼层
				this.buttons.setFloor(20 - (y / 30),Orientation);//更新电梯内的楼层显示
				if (20 - (y / 30)!= floor && dest[20 - (y / 30)]) {
					//未到达目标楼层但是到达目的地之一（出现这种情况是在移动过程中，在出发楼层到目标楼层间的楼层被加进dest数组）
					if(!buttons.buttons[20 - (y / 30 )].btn.isEnabled()){
						/*电梯内的乘客按了该楼层,在该楼层停留*/
						Orientation=0;//电梯设为静止态
						openDoor();//开门
						buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//当前层楼按键启用（被点击有效）*
						try {// 停1s
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							// 自动生成的捕获块
							e1.printStackTrace();
						}
						closeDoor();//关门
						if(!(!MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()
						&&MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled())){
							//除了该层未发出上行只发出下行请求的情况，该楼层都要移出dest数组
							dest[20 - (y / 30 )] = false;//当前楼层移出目的地数组
						}
					}
					else{
						/*电梯内的乘客未点击该楼层 */
						if (MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()
						&& MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()){
							/* 该层上下行按钮都未按下*/
							dest[20 - (y / 30 )] = false;//当前楼层移出目的地数组
						}else{
							if (!MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()) {//当前层上行按钮被按下
								MyBuilding.upButtons[20 - (y / 30)].btn.setEnabled(true);//当前层上行按钮复原，恢复点击有效
								Orientation = 0;//电梯设为静止态
								openDoor();
								buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//电梯内当前楼层数按键启用（被点击有效）*
								try {// 停留1s
									Thread.sleep(2000);
								} catch (InterruptedException e1) {
									// 自动生成的捕获块
									e1.printStackTrace();
								}
								closeDoor();
								dest[20 - (y / 30)] = false;//当前楼层移出目的地数组
								Orientation = 1;
							}
							if (!MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()) {//当前层下行按钮被按下
								System.out.println((20 - (y / 30 ))+"楼"+this.getFarthest()+"上行最远" );
								if(this.getFarthest() == 0){//当前层是上行方向最后一个目的地
									MyBuilding.downButtons[20 - (y / 30)].btn.setEnabled(true);//当前层下行按钮复原，恢复点击有效
									Orientation = 0;//电梯设为静止态
									openDoor();
									buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//电梯内当前楼层数按键启用（被点击有效）*
									try {// 停留1s
										Thread.sleep(2000);
									} catch (InterruptedException e1) {
									// 自动生成的捕获块
									e1.printStackTrace();
								}
								closeDoor();
								dest[20 - (y / 30)] = false;//当前楼层移出目的地数组
								Orientation = -1;
								return;//由this.getFarthest() == 0知floor层已不在dest中，不用继续走到floor层
								}
								else{
									dest[20 - (y / 30 )] = true;//当前楼层仍留在目的地数组，等待下行接应（或被其它电梯接应）
								}
							}
						}
					}



					/*if (MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()
							&& MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()) {
								//该层上下行按钮都未按下
								if(!buttons.buttons[20 - (y / 30 )].btn.isEnabled()){
								//是电梯内的乘客添加的目的地楼层,在该楼层停留
									Orientation=0;//电梯设为静止态
									openDoor();//开门
									buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//当前层楼按键启用（被点击有效）*
									try {// 停1s
										Thread.sleep(2000);
									} catch (InterruptedException e1) {
										// 自动生成的捕获块
										e1.printStackTrace();
									}
									closeDoor();//关门
								}
						dest[20 - (y / 30 )] = false;//当前楼层移出目的地数组
					} else {
						if (!MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()) {//当前层上行按钮被按下
							MyBuilding.upButtons[20 - (y / 30)].btn.setEnabled(true);//当前层上行按钮复原，恢复点击有效
							Orientation = 0;//电梯设为静止态
							openDoor();
							buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//电梯内当前楼层数按键启用（被点击有效）*
							try {// 停留1s
								Thread.sleep(2000);
							} catch (InterruptedException e1) {
								// 自动生成的捕获块
								e1.printStackTrace();
							}
							closeDoor();
							dest[20 - (y / 30)] = false;//当前楼层移出目的地数组
							Orientation = 1;
						}
						if (!MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()) {//当前层下行按钮被按下
							if (!buttons.buttons[20 - (y / 30 )].btn.isEnabled()) {//电梯内乘客也按钮了该楼层按键
								dest[20 - (y / 30)] = false;
								Orientation = 0;
								openDoor();
								buttons.buttons[20 - (y / 30)].btn.setEnabled(true);
								try {// 等待1s
									Thread.sleep(2000);
								} catch (InterruptedException e1) {
									// 自动生成的捕获块
									e1.printStackTrace();
								}
								closeDoor();
								MyBuilding.downButtons[20 - (y / 30)].btn.setEnabled(true);
								Orientation = -1;
							} else {
								dest[20 - (y / 30 )] = true;
							}
						}
					}*/
				}
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		/*电梯位于目标楼层上方*/
		while (y < 600 - floor * 30) {
			Orientation=-1;//电梯处于向下移动状态
			if (alarm) {
				return;
			}
			y += 1;
			this.repaint();
			if(y % 30 == 0) {//电梯到达某一楼层
				this.buttons.setFloor(20 - (y / 30),Orientation);//更新电梯内数字楼层显示
				if (20 - (y / 30) != floor && dest[20 - (y / 30)]) {
					//未到达目标楼层但是到达目的地之一，停留
					if(!buttons.buttons[20 - (y / 30 )].btn.isEnabled()){
						/*电梯内的乘客按了该楼层,在该楼层停留*/
						Orientation=0;//电梯设为静止态
						openDoor();//开门
						buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//当前层楼按键启用（被点击有效）*
						try {// 停1s
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							// 自动生成的捕获块
							e1.printStackTrace();
						}
						closeDoor();//关门
						if(!(!MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()
						&&MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled())){
							//除了该层未发出下行只发出上行请求的情况，该楼层都要移出dest数组
							dest[20 - (y / 30 )] = false;//当前楼层移出目的地数组
						}
					}
					else{
						/*电梯内的乘客未点击该楼层 */
						if (MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()
						&& MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()){
							/* 该层上下行按钮都未按下*/
							dest[20 - (y / 30 )] = false;//当前楼层移出目的地数组
						}else{
							if (!MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()) {//当前层下行按钮被按下
								MyBuilding.downButtons[20 - (y / 30)].btn.setEnabled(true);//当前层下行按钮复原，恢复点击有效
								Orientation = 0;//电梯设为静止态
								openDoor();
								buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//电梯内当前楼层数按键启用（被点击有效）*
								try {// 停留1s
									Thread.sleep(2000);
								} catch (InterruptedException e1) {
									// 自动生成的捕获块
									e1.printStackTrace();
								}
								closeDoor();
								dest[20 - (y / 30)] = false;//当前楼层移出目的地数组
								Orientation = -1;
							}
							if (!MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()) {//当前层上行按钮被按下
									System.out.println((20 - (y / 30 ))+"楼"+this.getFarthest()+"下行最远" );
								if(this.getFarthest() == 0){//当前层是下行方向最后一个目的地
									System.out.println((20 - (y / 30 ))+"楼下行最后一个目的地" );
									MyBuilding.upButtons[20 - (y / 30)].btn.setEnabled(true);//当前层上行按钮复原，恢复点击有效
									Orientation = 0;//电梯设为静止态
									openDoor();
									buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//电梯内当前楼层数按键启用（被点击有效）*
									try {// 停留1s
										Thread.sleep(2000);
									} catch (InterruptedException e1) {
									// 自动生成的捕获块
									e1.printStackTrace();
									}
									closeDoor();
									dest[20 - (y / 30)] = false;//当前楼层移出目的地数组
									Orientation = 1;
									return;//由this.getFarthest() == 0知floor层已不在dest中，不用继续走到floor层
								}
								else{
									dest[20 - (y / 30 )] = true;//当前楼层仍留在目的地数组，等待上行接应（或被其它电梯接应）
								}
							}
						}
					}




					/* 
					if (MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()
							&& MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()) {
						//该层上下行按钮都未按下
						if(!buttons.buttons[20 - (y / 30 )].btn.isEnabled()){
							//是电梯内的乘客添加的目的地楼层,在该楼层停留
								Orientation=0;//电梯设为静止态
								openDoor();//开门
								buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//当前层楼按键启用（被点击有效）*
								try {// 停1s
									Thread.sleep(2000);
								} catch (InterruptedException e1) {
									// 自动生成的捕获块
									e1.printStackTrace();
								}
								closeDoor();//关门
							}
						dest[20 - (y / 30 )] = false;//当前楼层移出目的地数组
					} else {
						if (!MyBuilding.downButtons[20 - (y / 30)].btn.isEnabled()) {//该楼层发出下行请求
							MyBuilding.downButtons[20 - (y / 30)].btn.setEnabled(true);//该层下行按钮恢复使用
							Orientation = 0;//电梯静止
							openDoor();
							buttons.buttons[20 - (y / 30)].btn.setEnabled(true);//电梯内该楼层数按钮恢复使用
							try {// wait for a second
								Thread.sleep(2000);
							} catch (InterruptedException e1) {
								// 自动生成的捕获块
								e1.printStackTrace();
							}
							closeDoor();
							dest[20 - (y / 30 + 1)] = false;
							Orientation = -1;//电梯下行
						}
						if (!MyBuilding.upButtons[20 - (y / 30)].btn.isEnabled()) {//该层发出上行请求
							if (this.getFarthest() == y) {
								dest[20 - (y / 30)] = false;
								Orientation = 0;//电梯静止
								openDoor();
								buttons.buttons[20 - (y / 30 )].btn.setEnabled(true);//电梯内该楼层数按钮恢复使用
								try {// d=等待1s
									Thread.sleep(2000);
								} catch (InterruptedException e1) {
									// 自动生成的捕获块
									e1.printStackTrace();
								}
								closeDoor();
								MyBuilding.upButtons[20 - (y / 30)].btn.setEnabled(true);
								Orientation = 1;//电梯上行
							} else {
								dest[20 - (y / 30)] = true;
							}
						}
					}*/
				}
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		/*电梯到达该楼层 */
		boolean tag = false;//当接受下行时变为true
		if (floor > 1 && (!MyBuilding.downButtons[floor].btn.isEnabled())) {//该楼层发出了下行请求
			if (Orientation==-1) {
				MyBuilding.downButtons[floor].btn.setEnabled(true);
				tag = true;
			} 
				else {
				if (this.getFarthest() == 0) {
					dest[20 - (y / 30)] = false;
					tag = true;
					MyBuilding.downButtons[floor].btn.setEnabled(true);
					Orientation=-1;
				}
			}
		}
		else if (floor < 20 && (!MyBuilding.upButtons[floor].btn.isEnabled())) {//floor楼层发出上行请求
			if(!tag) {
				if (Orientation==1) {
					MyBuilding.upButtons[floor].btn.setEnabled(true);
				} else if (Orientation==-1) {
					if (this.getFarthest() == 0) {
						dest[20 - (y / 30 )] = false;
						MyBuilding.upButtons[floor].btn.setEnabled(true);
						Orientation=1;
					}
				}
			}
		}
		else{//floor没有发出请求，是由乘客添加的目的地
			openDoor();//开门
			buttons.buttons[floor].btn.setEnabled(true);//当前层楼按键启用（被点击有效）*
			try {// 停1s
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// 自动生成的捕获块
				e1.printStackTrace();
			}
			closeDoor();//关门
		}
		dest[floor] = false;
		Orientation=0;
	}
	
	// 开门
	private void openDoor() {
		IsOpen = true;
		buttons.btnClose.btn.setEnabled(true);
		while (x > 15) {
			if (Orientation!=0) {
				break;
			}
			x -= 1;
			this.repaint();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//关门
	private void closeDoor() {
		while (x < 20) {
			x += 1;
			this.repaint();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		IsOpen = false;
	}

	//警报 
	private void alarming() {
		if (alarm) {
			for (int i = 1; i <= 20; i++) {
				buttons.buttons[i].btn.setDisabledIcon(new ImageIcon(this.getClass().getResource("/image/" + i + "A.png")));
			}
			buttons.btnOpen.btn.setDisabledIcon(new ImageIcon(this.getClass().getResource("/image/openA.png")));
			buttons.btnClose.btn.setDisabledIcon(new ImageIcon(this.getClass().getResource("/image/closeA.png")));
			
			for (int i = 1; i <= 20; i++) {//电梯内所有楼层数按键禁用
				buttons.buttons[i].btn.setEnabled(false);
			}
			buttons.btnOpen.btn.setEnabled(false);
			buttons.btnClose.btn.setEnabled(false);
		}
	}

	// 单部电梯的运行
	public void dispatch() {
		while ((!IsOpen) && (!alarm)) {
			buttons.btnOpen.btn.setEnabled(true);//电梯开门按键有效
			for (int i = 1; i <= 20; i++) {
				if (dest[i]) {//存在目的地
					moveToFloor(i);//移动到目的地
					System.out.println("moveto dest"+i);
					openDoor();//到达后开门
					buttons.buttons[i].btn.setEnabled(true);//恢复目的地楼层数按键有效
					try {//等待一秒
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// 自动生成捕捉块
						e1.printStackTrace();
					}
					closeDoor();
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		closeDoor();
	}

	// 返回电梯纵坐标
	public int getMyY() {
		return y;
	}

	// 返回电梯所在楼层
	public int getCurrentFloor() {
		int floor = (600 - y) / 30;
		return floor;
	}

	// 返回电梯目的地楼层中与当前电梯所在楼层最远的坐标距离
	public int getFarthest() {
		int currentFloor = getCurrentFloor();
		int farthest=0,totallen=0,waitTime=0;//最远距离和停留次数
		if (Orientation==1) {//电梯上行
			for (int i = currentFloor+1; i <=20 ; i++) {
				if (dest[i]) {
					farthest= getMyY() - (600 - 30 * i);
					waitTime++;
				}
			}
			int lowest=currentFloor;//当前在dest数组中且位于currentFloor最下方的楼层
			for(int i=currentFloor;i>=1;i--){//可能存在电梯上行又后到下面的楼层去接应的情况
				if (dest[i]) {
					if(i<lowest){
						lowest=i;
					}
					waitTime++;
				}
			}
			if(lowest<currentFloor){
				totallen=farthest*2;
				totallen+= 600 - 30 * lowest - getMyY();
			}
		}
		if (Orientation==-1) {//电梯下行
			for (int i = currentFloor-1; i >=0 ; i--) {
				if (dest[i]) {
					farthest= 600 - 30 * i - getMyY();
				}
			}
			int highest=currentFloor;//当前在dest数组中且位于currentFloor最上方的楼层
			for(int i = currentFloor; i <=20 ; i++){//可能存在电梯下行又后到上面的楼层去接应的情况
				if (dest[i]) {
					if(i>highest){
						highest=i;
					}
					waitTime++;
				}
			}
			if(highest>currentFloor){
				totallen=farthest*2;
				totallen+= getMyY() - (600 - 30 * highest);
			}
		}
		timeTask=totallen*5+waitTime*2000;
		System.out.println(timeTask+" "+totallen);
		return farthest;
	}
}