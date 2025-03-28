import sys

# pyqt5
from PyQt5 import QtCore,QtGui
from PyQt5.QtGui import QIcon, QStandardItem, QTextOption, QCursor
from PyQt5.QtCore import QRect, QModelIndex
from PyQt5.QtWidgets import QWidget, QPushButton, QApplication, QLabel, QVBoxLayout, QHBoxLayout, \
    QPlainTextEdit, QMainWindow, QMessageBox, QInputDialog, QTreeView, QAbstractItemView,\
    QMenu,QListWidget,QListWidgetItem

from file_system import *
from datetime import  datetime

# QSS样式
from qt_material import apply_stylesheet

SYSTEM_INFO = "file_system_info"


class FileSystemUI(QMainWindow):
    def __init__(self):
        super().__init__()
        self.fs = FileSystem(SYSTEM_INFO)
        # 当前目录路径
        self.current_directory = self.fs.current_directory
        self.cur_path = self.current_directory.path
        self.cur_selected_file = None
        self.cur_selected_dir = None

        self.setup_ui()

    def setup_ui(self):
        # 窗体信息
        self.resize(800, 600)
        self.setWindowTitle("文件系统模拟")
        self.setWindowIcon(QIcon('imgs/file_system_icon.png'))

        # 菜单栏
        menuBar = self.menuBar()
        fileMenu = menuBar.addMenu("文件")
        fileMenu.addAction(QIcon('imgs/format.png'), "格式化", self.format)
        fileMenu.addAction(QIcon('imgs/save.png'), "保存", self.save)

        createMenu = menuBar.addMenu("创建")
        createMenu.addAction(QIcon('imgs/create_file.png'), "创建文件", self.create_file)
        createMenu.addAction(QIcon('imgs/create_dir.png'), "创建文件夹", self.create_dir)

        deleteMenu = menuBar.addMenu("删除")
        deleteMenu.addAction(QIcon('imgs/delete_file.png'), "删除文件", self.delete_file)
        deleteMenu.addAction(QIcon('imgs/delete_dir.png'), "删除文件夹", self.delete_dir)

        renameMenu = menuBar.addMenu("重命名")
        renameMenu.addAction(QIcon('imgs/rename.png'), "重命名文件", self.rename_file)
        renameMenu.addAction(QIcon('imgs/rename.png'), "重命名文件夹", self.rename_dir)

        infoMenu = menuBar.addMenu('说明')
        infoMenu.addAction(QIcon('imgs/about.png'), "关于..", self.about)
        infoMenu.addAction(QIcon('imgs/tutorial.png'), "用法", self.tutorial)

        # 设置layout
        widget = QWidget()
        v1 = QVBoxLayout()
        widget.setLayout(v1)
        self.setCentralWidget(widget)

        # 上方路径栏
        #self.cur_path=str(self.cur_path)
        self.path_label = QLabel(self.cur_path)
        self.update_path_label()
        # 返回按钮
        self.up_button = QPushButton("↑返回上一级目录")
        # Connect button click event to go_up() method
        self.up_button.clicked.connect(self.go_up)
        v1.addWidget(self.path_label)
        v1.addWidget(self.up_button)

        self.listWidget = QListWidget()
        self.update_file_list_model()

        h1 = QHBoxLayout()
        h1.addWidget(self.listWidget)
        v1.addLayout(h1)



        # 右侧文件内容展示
        # 标签
        label = QLabel("当前打开文件内容")
        label.setAlignment(QtCore.Qt.AlignCenter)
        # 文本编辑框
        self.text_edit = QPlainTextEdit()
        self.text_edit.setWordWrapMode(QTextOption.WrapAnywhere)
        # 保存按钮
        self.save_button = QPushButton("save")
        self.save_button.clicked.connect(self.save_file)

        v2 = QVBoxLayout()
        v2.addWidget(label)
        v2.addWidget(self.text_edit)
        v2.addWidget(self.save_button)
        h1.addLayout(v2)

        # 底部文件/文件夹信息
        self.footer = QLabel()
        v1.addWidget(self.footer)

    # 鼠标点击左侧条目时，更新选中的文件夹/文件，同时更新各部分组件
    def click_item(self, cur: QModelIndex):
        #reverse_cur_path = []#保存回溯路径
        file_order = self.listWidget.row(cur)  # 获取当前选中文件在列表中的行索引
        print(file_order)

        sub_dir_num = len(self.current_directory.subDirectories)
        if file_order >= sub_dir_num:
            # 所选项为文件
            self.cur_selected_file = self.current_directory.files[file_order - sub_dir_num]
            self.cur_selected_dir = None
            print("file ", self.cur_selected_file.name)
        else:
            # 所选项为文件夹
            self.cur_selected_dir = self.current_directory.subDirectories[sub_dir_num-1]
            self.cur_selected_file = None
            print("dir ", self.cur_selected_dir.name)

        # update path label
        self.update_path_label()
        # update footer
        self.update_footer()
        # update text edit
        self.update_text_edit()

        # set button enable
        if self.cur_selected_file is not None:
            self.save_button.setEnabled(True)
        else:
            self.save_button.setEnabled(False)

    # 构建tree model所需函数
    def __append_items(self, model):
        for dir in self.current_directory.subDirectories:
            child_item = QStandardItem(dir.name)
            model.appendRow(child_item)
        for file in self.current_directory.files:
            model.appendRow(QStandardItem(file.name))


    # 更新所有部件
    def update_all_components(self):
        self.update_file_list_model()
        self.update_footer()
        self.update_path_label()
        self.update_text_edit()

    def update_file_list_model(self):
        self.listWidget.clear()  # 清空列表框

        # 获取当前路径下的文件和文件夹列表
        file_list = self.fs.current_directory.files
        dir_list = self.fs.current_directory.subDirectories

        # 显示文件和文件夹列表
        for item in dir_list:
            list_item = QListWidgetItem(item.name)
            list_item.setIcon(QtGui.QIcon("imgs/folder_icon.png"))  # 设置文件夹图标
            self.listWidget.addItem(list_item)
        for item in file_list:
            list_item = QListWidgetItem(item.name)
            self.listWidget.addItem(list_item)


        # 增加点击事件
        self.listWidget.itemClicked.connect(self.click_item)
        # 增加右击点击事件
        self.listWidget.setContextMenuPolicy(QtCore.Qt.CustomContextMenu)
        self.listWidget.customContextMenuRequested.connect(self.right_click_item)
        # 增加双击事件
        self.listWidget.itemDoubleClicked.connect(self.double_click_item)
        # 设为不可更改
        self.listWidget.setEditTriggers(QAbstractItemView.NoEditTriggers)

        # 设置滚动条策略
        self.listWidget.setVerticalScrollBarPolicy(QtCore.Qt.ScrollBarAlwaysOn)
        self.listWidget.setHorizontalScrollBarPolicy(QtCore.Qt.ScrollBarAlwaysOff)
        self.listWidget.setWrapping(False)  # 禁止自动换行
        self.listWidget.setIconSize(QtCore.QSize(16, 16))  # 设置图标尺寸
        self.listWidget.setSelectionMode(QAbstractItemView.SingleSelection)  # 单选模式

        self.listWidget.setFocus()  # 设置焦点在列表框上

    def update_path_label(self):
        self.path_label.setText(self.cur_path)

    def update_text_edit(self):
        if self.cur_selected_file is not None:
            self.text_edit.setEnabled(True)
            self.text_edit.setPlainText(self.fs.open_file(self.cur_selected_file))
        else:
            self.text_edit.setPlainText("尚未在左侧选中文件")
            self.text_edit.setEnabled(False)

    def update_footer(self):
        if self.cur_selected_dir is not None:
            self.footer.setText("(selected dir) " + self.cur_selected_dir.name + "   |   contains " + str(
                self.cur_selected_dir.size()) + " items\n" +
                                "created in " + str(self.cur_selected_dir.create_time) + ", modified in " + str(
                self.cur_selected_dir.modify_time))
            if self.cur_selected_file is not None:
                self.footer.setText(self.footer.text() +
                                    "\n(selected file) " + self.cur_selected_file.name + "   |   length: " + str(
                    self.cur_selected_file.length) + "\n" +
                                    "created in " + str(self.cur_selected_file.create_time) + ", modified in " + str(
                    self.cur_selected_file.modify_time))
        else:
            self.footer.setText("not selected")

    def save_file(self):
        data = self.fs.open_file(self.cur_selected_file)

        if self.text_edit.toPlainText() == data:
            QMessageBox.warning(self, "警告", "无任何变动！")
            return

        ans = QMessageBox.question(self, '确认', "是否保存？", QMessageBox.Yes | QMessageBox.No)

        if ans == QMessageBox.Yes:
            self.fs.write_file(self.text_edit.toPlainText(), self.cur_selected_file)
            self.update_footer()

    # 双击打开文件
    def double_click_item(self):
        if self.cur_selected_dir is None:
            return

        self.open_dir()

    # 右键点击，同时会触发click item
    # 增加右键菜单 根据选中文件或文件夹 跳出不同选项
    def right_click_item(self):
        if self.cur_selected_dir is None and self.cur_selected_file is None:
            return

        right_click_menu = QMenu()
        if self.cur_selected_file is not None:
            #right_click_menu.addAction(QIcon('imgs/open_file.png'), "打开文件", self.open_file)
            right_click_menu.addAction(QIcon('imgs/delete_file.png'), "删除文件", self.delete_file)
            right_click_menu.addAction(QIcon('imgs/rename.png'), "重命名文件", self.rename_file)
        elif self.cur_selected_dir is not None:
            right_click_menu.addAction(QIcon('imgs/open_dir.png'), "打开文件夹", self.open_dir)
            right_click_menu.addAction(QIcon('imgs/rename.png'), "重命名文件夹", self.rename_dir)
            right_click_menu.addAction(QIcon('imgs/delete_dir.png'), "删除文件夹", self.delete_dir)

        right_click_menu.exec_(QCursor.pos())
        right_click_menu.show()

    # 关于
    def about(self):
        QMessageBox.about(self, '关于', '本项目为2023年操作系统课程第三次作业文件管理系统项目\n'
                                      '作者：2152343 何慧琳\n'
                                      '指导老师：张惠娟')

    # 说明
    def tutorial(self):
        QMessageBox.about(self, '教程', '上方菜单栏可进行文件/文件夹操作\n'
                                      '左侧文件列表可左键选中/右键操作\n'
                                      '右侧显示选中文件内容，点击save可保存')

    # 格式化
    def format(self):
        ans = QMessageBox.question(self, '确认', "是否格式化？", QMessageBox.Yes | QMessageBox.No)
        if ans == QMessageBox.Yes:
            self.fs.format()
            self.cur_selected_file = None
            self.cur_selected_dir = self.fs.root
            self.current_directory = self.fs.root
            self.cur_path = self.cur_selected_dir.path
            self.update_all_components()

    def save(self):
        ans = QMessageBox.question(self, '确认', "保存到本地文件？", QMessageBox.Yes | QMessageBox.No)
        if ans == QMessageBox.Yes:
            self.fs.save(SYSTEM_INFO)

    def create_file(self):
        new_file_name, ok = QInputDialog.getText(self, '创建文件', '输入创建文件名：')
        if ok:
            if new_file_name == "":
                QMessageBox.warning(self, "警告", "文件名为空！")
            #elif self.cur_selected_dir is None:
                #QMessageBox.warning(self, "警告", "请先在左侧选中创建文件所在的文件夹！")
            if self.cur_selected_dir is None:
                self.cur_selected_dir = self.current_directory
            if len([x for x in self.cur_selected_dir.files if x.name == new_file_name]) > 0:
                QMessageBox.warning(self, "警告", "已有重复文件名！")
            else:
                self.fs.create_file(new_file_name)
                self.update_file_list_model()
                self.update_all_components()

    def create_dir(self):
        new_dir_name, ok = QInputDialog.getText(self, '创建文件夹', '输入创建文件夹名：')
        if ok:
            if new_dir_name == "":
                QMessageBox.warning(self, "警告", "文件夹名为空！")
            if self.cur_selected_dir is None:
                # QMessageBox.warning(self, "警告", "请先在左侧选中创建文件夹所在的文件夹！")
                self.cur_selected_dir = self.current_directory
            if len([x for x in self.cur_selected_dir.subDirectories if x.name == new_dir_name]) > 0:
                QMessageBox.warning(self, "警告", "已有重复文件夹名！")
            else:
                self.fs.create_directory(new_dir_name, self.cur_selected_dir, datetime.now())
                self.update_all_components()

    def delete_file(self):
        if self.cur_selected_file is None:
            QMessageBox.warning(self, "警告", "请先在左侧选中删除的文件！")
        else:
            ans = QMessageBox.question(self, '确认', "删除" + self.cur_selected_file.name + "？",
                                       QMessageBox.Yes | QMessageBox.No)
            if ans == QMessageBox.Yes:
                self.fs.delete_file(self.cur_selected_file,self.current_directory)
                self.cur_selected_file = None
                self.update_all_components()

    def delete_dir(self):
        if self.cur_selected_dir is None:
            QMessageBox.warning(self, "警告", "请先在左侧选中删除的文件夹！")
        elif self.cur_selected_dir == self.fs.root:
            QMessageBox.warning(self, "警告", "根文件夹不可删除！")
        else:
            ans = QMessageBox.question(self, '确认', "删除" + self.cur_selected_dir.name + "？",
                                       QMessageBox.Yes | QMessageBox.No)
            if ans == QMessageBox.Yes:
                self.fs.delete_directory(self.cur_selected_dir)
                self.cur_selected_dir = None
                self.cur_selected_file = None
                self.update_all_components()

    def open_dir(self):
        if self.cur_selected_dir is None:
            QMessageBox.warning(self, "警告", "请先在左侧选中打开的文件夹！")
        elif self.cur_selected_dir == self.fs.root:
            QMessageBox.warning(self, "警告", "根文件夹不可打开！")
        else:
            self.cur_path = self.cur_selected_dir.path
            self.fs.open_directory(self.cur_selected_dir)
            self.current_directory = self.cur_selected_dir
            self.cur_selected_dir = None
            self.cur_selected_file = None
            self.update_all_components()

    def rename_file(self):
        if self.cur_selected_file is None:
            QMessageBox.warning(self, "警告", "请先在左侧选中重命名的文件！")
        else:
            new_file_name, ok = QInputDialog.getText(self, '重命名文件', '输入新文件名：')
            if ok:
                if new_file_name == "":
                    QMessageBox.warning(self, "警告", "文件名为空！")
                elif len([x for x in self.current_directory.files if x.name == new_file_name]) > 0:
                    QMessageBox.warning(self, "警告", "已有重复文件名！")
                else:
                    self.fs.rename_file(self.cur_selected_file, new_file_name, self.current_directory)
                    self.update_all_components()

    def rename_dir(self):
        if self.cur_selected_dir is None:
            QMessageBox.warning(self, "警告", "请先在左侧选中重命名的文件夹！")
        else:
            new_dir_name, ok = QInputDialog.getText(self, '重命名文件夹', '输入新文件夹名：')
            if ok:
                if new_dir_name == "":
                    QMessageBox.warning(self, "警告", "文件夹名为空！")
                elif len([x for x in self.current_directory.subDirectories if x.name == new_dir_name]) > 0:
                    QMessageBox.warning(self, "警告", "已有重复文件夹名！")
                else:
                    self.fs.rename_dir(self.cur_selected_dir, new_dir_name)
                    self.update_all_components()

    def go_up(self):
        if self.fs.current_directory.parentDirectory is not None:
            self.fs.current_directory = self.fs.current_directory.parentDirectory
            self.cur_path = self.current_directory.parentDirectory.path
            self.current_directory = self.fs.current_directory
            self.update_all_components()

    # 关闭窗口时弹出确认消息
    def closeEvent(self, event):
        self.save()


if __name__ == '__main__':
    app = QApplication(sys.argv)

    u = FileSystemUI()

    # setup stylesheet
    apply_stylesheet(app, theme='light_blue_500.xml')

    u.show()
    sys.exit(app.exec_())
