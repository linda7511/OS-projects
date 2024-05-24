package component;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

public class Buttons extends JLabel {
	public MyButton[] buttons = new MyButton[24];//数字按键数组
	public MyButton btnOpen;//开门按键
	public MyButton btnClose;//关门按键
	public MyButton btnAlarm;//警报按键
	public JLabel floorNum;//楼层的数字显示

	// 构造方法
	public Buttons() {
		MyButton.num_tot = 0;
		// 设置标签属性
		this.setLayout(null);//将组件面板设置为固定布局
		this.setSize(80, 510);//设置电梯内部按钮背景面板的尺寸
		this.setOpaque(true);//设置不透明
		this.setBackground(new Color(230, 230, 250));//设置背景颜色
		this.setVisible(true);//set visible
		// 添加标签
		String str = "0 1";//电梯初始位置显示为01

		floorNum = new JLabel(str, JLabel.CENTER);//楼层数字显示
		floorNum.setForeground(Color.RED);
		Font font = new Font("Arial", Font.PLAIN, 30);
		floorNum.setFont(font);//set font
		floorNum.setBackground(new Color(0, 0, 0));//设置数字背景颜色为黑色
		floorNum.setOpaque(true);//设置不透明
		floorNum.setBounds(5, 5, 70, 35);
		floorNum.setVisible(true);
		add(floorNum);

		// 添加数字按键
		for (int i = 1; i <= 20; i++) {
			MyButton btn = new MyButton("/image/" + i + ".png", "/image/" + i + "h.png", 5 + ((i -1)% 2) * 40,
					405 - ((i -1) / 2) * 40, 30, 30);
			add(btn.btn);
			buttons[i] = btn;
			System.out.println(btn.num + "楼添加");
		}
		//添加开门、关门、报警键
		btnOpen = new MyButton("/image/open.png", "/image/openH.png", 5, 445, 30, 30);
		add(btnOpen.btn);
		buttons[21] = btnOpen;

		btnClose = new MyButton("/image/close.png", "/image/closeH.png", 45, 445, 30, 30);
		add(btnClose.btn);
		buttons[22] = btnClose;

		btnAlarm = new MyButton("/image/alarm.png", "/image/alarmH.png", 25, 480, 30, 30);
		add(btnAlarm.btn);
		buttons[23] = btnAlarm;
	}

	public void setFloor(int floor,int orientation) {
		//设置楼层数字显示
		String str="",arrow="";
		if (floor == -1) {
			str = "ERR!";
		} else {
			if(orientation==1){
				arrow="↑";
			}
			else if(orientation==-1){
				arrow="↓";
			}
			if (floor < 10) {
			str = "0 " + floor+arrow;
		} else if (floor < 20) {
			str = "1 " + (floor - 10)+arrow;
		} else {
			str = "2 0"+arrow;
		}
	}
		this.floorNum.setText(str);
		return;
	}
}
