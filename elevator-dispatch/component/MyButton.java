package component;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class MyButton {
	static int num_tot = 0;//楼层总数
	public int num;//按键所在楼层
	public JButton btn = new JButton("");

	//构造方法
	/*public MyButton(){
		num=0
	}*/
	public MyButton(String path_nor, String path_high, int x, int y, int width, int height) {
		System.out.println(path_nor);
		num = num_tot;
		System.out.println(num);
		num_tot++;
		btn.setIcon(new ImageIcon(this.getClass().getResource(path_nor)));
		//设置当前按钮为平常态样式
		
		btn.setBounds(x, y, width, height);
		
		btn.setDisabledIcon(new ImageIcon(this.getClass().getResource(path_high)));
		//点亮态图标失效
		
		btn.setPressedIcon(new ImageIcon(this.getClass().getResource(path_high)));
		//当图标被点击时设置当前按钮为点亮态样式
	}
}
