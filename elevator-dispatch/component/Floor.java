package component;

import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import javax.swing.JLabel;

public class Floor extends JLabel {
	public JLabel floor;//楼层标签
	static int cnt;//楼层数

	//构造方法，在图形界面打印楼层
	public Floor() {
		setLayout(null);
		Font font = new Font("宋体", Font.PLAIN, 18);
		floor = new JLabel( (20 - cnt) + "楼");
		floor.setFont(font);
		floor.setOpaque(true);
		floor.setBackground(Color.white);
		if (cnt % 2 == 0) {
			floor.setBorder(BorderFactory.createEtchedBorder());
		} 
		floor.setBounds(0, cnt * 30, 950, 30);
		floor.setVisible(true);
		cnt++;
	}
}
