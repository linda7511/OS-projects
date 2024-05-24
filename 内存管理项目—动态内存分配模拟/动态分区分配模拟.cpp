#include <iostream>
#include <iomanip>
using namespace std;

int mem[640], rest_size = 640;

int num_len(int num) //������λ�������ڼ�������Ű�
{
	int ans=1;
	while (num >= 10) {
		num /= 10;
		ans++;
	}
	return ans;
}

void print(int all=0)//allΪ0ʱ��ӡ�������԰��ڴ����ͼ��allΪ1ʱ��ӡ��ϸ�ڴ�������
{
	int start = 0, len = 1;
	if (!all) {//��ӡ�������԰��ڴ����ʾ��ͼ
		cout << "|";
		for (int i = 0; i < 640; i++) {
			if (i < 639 && mem[i] == mem[i + 1])
				len++;
			else {
				if (mem[i] == 0) {
					int filllen = (len - (7 + num_len(len)))/10;
					for (int j = 0; j < filllen / 2 - filllen % 2 == 0 ? 0 : 1; j++)
						cout << "-";
					cout <<  "���У�" << len << "k��";
					for (int j = 0; j < filllen / 2; j++)
						cout << "-";
					cout << "|";
				}
				else {
					int filllen = (len - (9 + num_len(len)+num_len(mem[i])))/10;
					for (int j = 0; j < filllen / 2 - filllen % 2 == 0 ? 0 : 1; j++)
						cout << "-";
					cout <<  mem[i] << "����ҵ��" << len << "k��";
					for (int j = 0; j < filllen / 2; j++)
						cout << "-";
					cout << "|";
				}
				len = 1;
			}
		}
		cout<< endl;
	}
	else {//��ӡ��ϸ�ڴ�����
		cout << "|---------------------��ǰ�ڴ����������£�---------------------|" << endl;
		cout << "|----------------------------------------------------------------|" << endl;
		cout << "|    ��ʼ��ַ    |    �ռ��С    |     ״̬      |    ��ҵ��    |" << endl;
		cout << "|----------------------------------------------------------------|" << endl;
		int start = 0;
		for (int i = 0; i < 640; i++) {
			if (i < 639 && mem[i] == mem[i + 1])
				len++;
			else {
				/*��ӡһ��*/
				cout << "|";
				int filllen = 16 - (1 + num_len(start));
				/*��ӡ��ʼ��ַ��*/
				for (int j = 0; j < filllen / 2+filllen%2==0?0:1; j++)
					cout << " ";
				cout << start << "k" << setw(filllen / 2) << " ";
				cout << "|";
				/*��ӡ�ռ��С��*/
				filllen = 16 - (1 + num_len(len));
				for (int j = 0; j < filllen / 2 + (filllen % 2 == 0 ? 0 : 1) ; j++)
					cout << " ";
				cout << len << "k" << setw(filllen / 2) << " ";
				cout << "|";
				/*��ӡ״̬������ҵ����*/
				if (mem[i] == 0) 
					cout << "     ����      |              |";
				else {
					cout << "     �ѷ���    |";
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

/*�״���Ӧ�㷨*/
void first_fit(int num,int size)
{
	if (size > 0) {//��������
		int len=0,start=-1;
		for (int i = 0; i < 640; i++) {
			if (mem[i] == 0){//�����������ۼ�
				len++;
				if (start == -1)//�������
					start = i;
				if (len == size) {//��ǰ���������������´�������
					for (int j = start; j <= i; j++)//���ÿ��������������
						mem[j] = num;
					rest_size -= size;
					print();
					return;
				}
			}
			else {//�����ǿ���������ԭlen��start
				len = 0;
				start = -1;
			}
		}
		cout << "����ʧ�ܣ���ǰ�ڴ�ռ�Ų��¸ý��̡�" << endl;
	}
	else {//size<0��ʾ��������
		for (int i = 0; i < 640; i++) {
			if (mem[i] == num) {
				mem[i]=0;
				rest_size++;
			}
		}
		print();
	}
}

/*�����Ӧ�㷨*/
void best_fit(int num,int size)
{
	if (size > 0) {//��������
		int len = 0, start = -1,best=-1,minlen=640;
		for (int i = 0; i < 640; i++) {
			if (mem[i] == 0&&i<639) {//�ۼ����������ֱ�������ǿ�����
				len++;
				if (start == -1)//���¿��������
					start = i;
			}
			else {//�����ǿ��������Ըս����Ŀ����������ж�
				if (len == size) {//�ս����Ŀ�������С�պõ���size����ȻΪ�����Ӧ������
					for (int j = start; j < i; j++)
						mem[j] = num;
					rest_size -= size;
					print();
					return;
				}
				else if (len > size && len < minlen) {//ѡ�������ɽ�������С�Ŀ�����
					minlen = len;
					best = start;
				}
				//��ԭlen��start
				len = 0;
				start = -1;
			}
		}
		if(best==-1)
			cout << "����ʧ�ܣ���ǰ�ڴ�ռ�Ų��¸ý��̡�" << endl;
		else {
			for (int j = best;j<best+size ; j++)
				mem[j] = num;
			rest_size -= size;
			print();
			return;
		}
	}
	else {//size<0��ʾ��������
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
	cout << "|------------------------------�˵�------------------------------|" << endl;
	cout << "|1.������̷���ռ�----------------------------------------------|" << endl;
	cout << "|2.�������̻��տռ�----------------------------------------------|" << endl;
	cout << "|3.��ӡ�ڴ��������----------------------------------------------|" << endl;
	cout << "|4.�˳���ǰ�㷨--------------------------------------------------|" << endl;
	cout << "|----------------------------------------------------------------|" << endl;
	int choice,num,size;
	while (1) {
		cout << "������˵���ţ�";
		while (1) {//���봦��
			cin >> choice;
			if (choice == 1 || choice == 2 || choice == 3||choice==4)
				break;
			else {
				cout << "����������������룺";
				cin.clear();
				cin.ignore();
			}
		}
		if (choice == 1) {
			cout << "������Ҫ�����Ľ�����ţ�";
			while (1) {//���봦��
				cin >> num;
				if (cin.fail()) {
					cout << "����������������룺";
					cin.clear();
					cin.ignore();
				}
				else
					break;
			}
			cout << "������Ҫ�����Ľ��̴�С��";
			while (1) {//���봦��
				cin >> size;
				if (size>0)
					break;
				else {
					cout << "����������������룺";
					cin.clear();
					cin.ignore();
				}
			}
			if (size > rest_size) 
				cout << "��ǰ�ڴ��ѷŲ��¸ý���" << endl;
			else {
				if (fb == 1)
					first_fit(num, size);
				else
					best_fit(num, size);
			}
		}
		else if(choice==2){
			cout << "������Ҫ�����Ľ�����ţ�";
			cin >> num;
			if (fb == 1)
				first_fit(num, -1);
			else
				best_fit(num, -1);
		}
		else if(choice==3){//��ӡ�ڴ��������
			print(1);
		}
		else {//�˳���ǰ�㷨
			memset(mem, 0, sizeof(mem));//����ڴ����
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
		cout << "|--------------��ӭ�����ڴ����-��̬��������ģ��ϵͳ-------------|" << endl;
		cout << "|----------------------------------------------------------------|" << endl;
		cout << "|�����㷨��------------------------------------------------------|" << endl;
		cout << "|1.�״���Ӧ�㷨--------------------------------------------------|" << endl;
		cout << "|2.�����Ӧ�㷨--------------------------------------------------|" << endl;
		cout << "|----------------------------------------------------------------|" << endl;
		cout << "������1/2ѡ������㷨��";
		while (1) {//���봦��
			cin >> first_or_best;
			if (first_or_best == 1 || first_or_best == 2)
				break;
			else {
				cout << "����������������룺";
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