#include <iostream>
#include <iomanip>
using namespace std;

int mem[640], rest_size = 640;

int num_len(int num) //求整数位数，用于计算输出排版
{
	int ans=1;
	while (num >= 10) {
		num /= 10;
		ans++;
	}
	return ans;
}

void print(int all=0)//all为0时打印横条简略版内存分配图，all为1时打印详细内存分配情况
{
	int start = 0, len = 1;
	if (!all) {//打印横条简略版内存分配示意图
		cout << "|";
		for (int i = 0; i < 640; i++) {
			if (i < 639 && mem[i] == mem[i + 1])
				len++;
			else {
				if (mem[i] == 0) {
					int filllen = (len - (7 + num_len(len)))/10;
					for (int j = 0; j < filllen / 2 - filllen % 2 == 0 ? 0 : 1; j++)
						cout << "-";
					cout <<  "空闲（" << len << "k）";
					for (int j = 0; j < filllen / 2; j++)
						cout << "-";
					cout << "|";
				}
				else {
					int filllen = (len - (9 + num_len(len)+num_len(mem[i])))/10;
					for (int j = 0; j < filllen / 2 - filllen % 2 == 0 ? 0 : 1; j++)
						cout << "-";
					cout <<  mem[i] << "号作业（" << len << "k）";
					for (int j = 0; j < filllen / 2; j++)
						cout << "-";
					cout << "|";
				}
				len = 1;
			}
		}
		cout<< endl;
	}
	else {//打印详细内存分配表
		cout << "|---------------------当前内存分配情况如下：---------------------|" << endl;
		cout << "|----------------------------------------------------------------|" << endl;
		cout << "|    起始地址    |    空间大小    |     状态      |    作业号    |" << endl;
		cout << "|----------------------------------------------------------------|" << endl;
		int start = 0;
		for (int i = 0; i < 640; i++) {
			if (i < 639 && mem[i] == mem[i + 1])
				len++;
			else {
				/*打印一行*/
				cout << "|";
				int filllen = 16 - (1 + num_len(start));
				/*打印起始地址栏*/
				for (int j = 0; j < filllen / 2+filllen%2==0?0:1; j++)
					cout << " ";
				cout << start << "k" << setw(filllen / 2) << " ";
				cout << "|";
				/*打印空间大小栏*/
				filllen = 16 - (1 + num_len(len));
				for (int j = 0; j < filllen / 2 + (filllen % 2 == 0 ? 0 : 1) ; j++)
					cout << " ";
				cout << len << "k" << setw(filllen / 2) << " ";
				cout << "|";
				/*打印状态栏和作业号栏*/
				if (mem[i] == 0) 
					cout << "     空闲      |              |";
				else {
					cout << "     已分配    |";
					filllen = 14 - num_len(mem[i]);
					for (int j = 0; j < filllen / 2 + filllen % 2 == 0 ? 0 : 1; j++)
						cout << " ";
					cout << mem[i] << setw(filllen / 2) << " ";
					cout << "|";
				}
				cout << endl;
				len = 1;
				start = i + 1;
			}
		}
		cout << endl;

	}
}

/*首次适应算法*/
void first_fit(int num,int size)
{
	if (size > 0) {//创建进程
		int len=0,start=-1;
		for (int i = 0; i < 640; i++) {
			if (mem[i] == 0){//空闲区长度累加
				len++;
				if (start == -1)//更新起点
					start = i;
				if (len == size) {//当前空闲区已能容纳下创建进程
					for (int j = start; j <= i; j++)//将该空闲区分配给进程
						mem[j] = num;
					rest_size -= size;
					print();
					return;
				}
			}
			else {//遇到非空闲区，复原len和start
				len = 0;
				start = -1;
			}
		}
		cout << "分配失败！当前内存空间放不下该进程。" << endl;
	}
	else {//size<0表示撤销进程
		for (int i = 0; i < 640; i++) {
			if (mem[i] == num) {
				mem[i]=0;
				rest_size++;
			}
		}
		print();
	}
}

/*最佳适应算法*/
void best_fit(int num,int size)
{
	if (size > 0) {//创建进程
		int len = 0, start = -1,best=-1,minlen=640;
		for (int i = 0; i < 640; i++) {
			if (mem[i] == 0&&i<639) {//累加整块空闲区直至遇到非空闲区
				len++;
				if (start == -1)//更新空闲区起点
					start = i;
			}
			else {//遇到非空闲区，对刚结束的空闲区进行判断
				if (len == size) {//刚结束的空闲区大小刚好等于size，必然为最佳适应，分配
					for (int j = start; j < i; j++)
						mem[j] = num;
					rest_size -= size;
					print();
					return;
				}
				else if (len > size && len < minlen) {//选择能容纳进程且最小的空闲区
					minlen = len;
					best = start;
				}
				//复原len和start
				len = 0;
				start = -1;
			}
		}
		if(best==-1)
			cout << "分配失败！当前内存空间放不下该进程。" << endl;
		else {
			for (int j = best;j<best+size ; j++)
				mem[j] = num;
			rest_size -= size;
			print();
			return;
		}
	}
	else {//size<0表示撤销进程
		for (int i = 0; i < 640; i++) {
			if (mem[i] == num) {
				mem[i] = 0;
				rest_size++;
			}
		}
		print();
	}
}

void ui(int fb)
{
	cout << "|------------------------------菜单------------------------------|" << endl;
	cout << "|1.输入进程分配空间----------------------------------------------|" << endl;
	cout << "|2.撤销进程回收空间----------------------------------------------|" << endl;
	cout << "|3.打印内存分配详情----------------------------------------------|" << endl;
	cout << "|4.退出当前算法--------------------------------------------------|" << endl;
	cout << "|----------------------------------------------------------------|" << endl;
	int choice,num,size;
	while (1) {
		cout << "请输入菜单序号：";
		while (1) {//输入处理
			cin >> choice;
			if (choice == 1 || choice == 2 || choice == 3||choice==4)
				break;
			else {
				cout << "输入错误，请重新输入：";
				cin.clear();
				cin.ignore();
			}
		}
		if (choice == 1) {
			cout << "请输入要创建的进程序号：";
			while (1) {//输入处理
				cin >> num;
				if (cin.fail()) {
					cout << "输入错误，请重新输入：";
					cin.clear();
					cin.ignore();
				}
				else
					break;
			}
			cout << "请输入要创建的进程大小：";
			while (1) {//输入处理
				cin >> size;
				if (size>0)
					break;
				else {
					cout << "输入错误，请重新输入：";
					cin.clear();
					cin.ignore();
				}
			}
			if (size > rest_size) 
				cout << "当前内存已放不下该进程" << endl;
			else {
				if (fb == 1)
					first_fit(num, size);
				else
					best_fit(num, size);
			}
		}
		else if(choice==2){
			cout << "请输入要撤销的进程序号：";
			cin >> num;
			if (fb == 1)
				first_fit(num, -1);
			else
				best_fit(num, -1);
		}
		else if(choice==3){//打印内存分配详情
			print(1);
		}
		else {//退出当前算法
			memset(mem, 0, sizeof(mem));//清空内存分配
			rest_size = 640;
			break;
		}
		cout << endl;
	}
}

int main()
{
	int first_or_best;
	while (1) {
		cout << "|--------------欢迎来到内存管理-动态分区分配模拟系统-------------|" << endl;
		cout << "|----------------------------------------------------------------|" << endl;
		cout << "|分配算法：------------------------------------------------------|" << endl;
		cout << "|1.首次适应算法--------------------------------------------------|" << endl;
		cout << "|2.最佳适应算法--------------------------------------------------|" << endl;
		cout << "|----------------------------------------------------------------|" << endl;
		cout << "请输入1/2选择分配算法：";
		while (1) {//输入处理
			cin >> first_or_best;
			if (first_or_best == 1 || first_or_best == 2)
				break;
			else {
				cout << "输入错误，请重新输入：";
				cin.clear();
				cin.ignore();
			}
		}
		if (first_or_best == 1)
			ui(1);
		else
			ui(2);
		cout << endl;
	}
	return 0;
}