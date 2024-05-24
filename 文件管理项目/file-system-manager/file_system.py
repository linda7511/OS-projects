import pickle
from datetime import datetime

block_num = 1024  # 物理块数
block_size = 10  # 物理块大小

class Block(list):
    def __init__(self):
        super(Block, self).__init__()
        for i in range(block_num):
            self.append("")


class FCB:
    def __init__(self, name, length, create_time,format="dat"):
        self.name = name
        self.length = length
        self.type = format
        self.index_table = [-1] * 14
        self.file = None
        self.label = "file"
        self.create_time = create_time
        self.modify_time = create_time

class Directory:
    def __init__(self, name,parentDirectory,create_time=datetime.now()):
        self.name = name
        self.files = []#包含文件
        self.subDirectories = []#子目录
        self.parentDirectory = parentDirectory#父目录
        self.label="directory"
        self.create_time = create_time
        self.modify_time = create_time
        if parentDirectory is None:
            self.path = self.name
        else:
            self.path = parentDirectory.path + ">" + self.name
    def size(self):
        return len(self.files) + len(self.subDirectories)

class FileSystem:
    def __init__(self, system_info_file=None):
        import os
        # 存在文件则直接读取
        if system_info_file and os.path.exists(system_info_file):
            with open(system_info_file, "rb") as f:
                self.root = pickle.load(f)
                self.current_directory = pickle.load(f)
                self.physical_blocks = pickle.load(f)
                self.blank = pickle.load(f)
        else:  # 否则手动创建
            self.root = Directory("root",None)  # 根目录
            self.current_directory = self.root  # 当前目录
            self.physical_blocks = [Block() for _ in range(block_num)]  # 创建物理块列表
            self.blank = [i for i in range(1024)]
            self.now_index = 0

    def create_file(self, name, length=0, format="dat"):
        if name not in self.current_directory.files:
            self.current_directory.files.append(FCB(name,length,datetime.now(), 0))


    def open_file(self, file):
        # 打开并读取文件数据
        if file.index_table[0] == -1:
            return ""
        j = 0
        data = ""
        while j < file.length:
            if j < 12:
                cursor = file.index_table[j]
            elif 12 <= j < 12 + 10:
                cursor = self.physical_blocks[file.index_table[j]][j-12]
            else:
                cursor = self.physical_blocks[self.physical_blocks[file.index_table[j]][(j - 12) // 10]][(j - 12) % 10]
            data += self.physical_blocks[cursor]
            j += 1
        return data

        # 写入并保存数据
    def write_file(self, data, file: FCB):
        cur_index = 0
        file.modify_time = datetime.now()
        file.length=0
        while data != "":
            j=file.length
            index = self.blank.pop(0)
            #将物理块号填入索引表
            if j == 121 or self.blank is None:
                raise AssertionError("don't have enough space!!")
            if j < 12:#直接索引
                file.index_table[j] = index
            elif 12 <= j < 12+10:#一级索引
                if j == 12:
                    file.index_table[j] = index
                    index = self.blank.pop(0)
                    self.physical_blocks[file.index_table[12]] = [-1] * 10
                self.physical_blocks[file.index_table[12]][j-12] = index
            else:#二级索引
                if j == 22:
                    file.index_table[13] = index
                    index = self.blank.pop(0)
                if (j - 12) % 10 == 0:
                    self.physical_blocks[file.index_table[13]] = [-1] * 10
                    self.physical_blocks[file.index_table[13]][(j - 22) // 10] = index
                    index2 = self.blank.pop(0)
                    self.physical_blocks[index] = [-1] * 10
                    self.physical_blocks[index][(j - 22) % 10] = index2
                    index = index2
                else:
                    self.physical_blocks[self.physical_blocks[file.index_table[13]][(j - 22) // 10]][(j - 22) % 10] = index
            file.length += 1
            self.physical_blocks[index]=data[:block_size]#将前block_size大小的数据赋给物理块
            data=data[block_size:]#将前block_size的数据从data中移除


    def delete_file(self, file ,directory):
        cursor = file.index_table[0]
        j = 0
        # 释放物理块
        while j < file.length:
            self.physical_blocks[cursor] = ""
            self.blank.append(cursor)#将该物理块号加入空闲块表
            j += 1
            if j < 12:
                cursor = file.index_table[j]
            elif 12 <= j < 12 + 10:
                cursor = self.physical_blocks[file.index_table[12]][j-12]
            else:
                cursor = self.physical_blocks[self.physical_blocks[file.index_table[13]][(j - 12) // 10]][(j - 12) % 10]
            if j == 22:
                #回收一级缩影块
                self.physical_blocks[file.index_table[12]] = ""
                self.blank.append(file.index_table[12])  # 将该物理块号加入空闲块表
            if j>22 and (j-12+1)%10 == 0:
                # 回收二级缩影块
                self.blank.append(self.physical_blocks[file.index_table[13]][(j - 12) // 10])  # 将该物理块号加入空闲块表
            if j == 121:
                self.physical_blocks[file.index_table[13]] = ""
                self.blank.append(file.index_table[13])  # 将该物理块号加入空闲块表
        # 从当前目录文件列表中删除文件
        directory.files.remove(file)

    def create_directory(self, name, parent_directory,create_time):
        for sub_dir in parent_directory.subDirectories:
            if sub_dir.name == name:
                print("目录名已存在！")
                return
        directory = Directory(name, parent_directory, create_time)
        directory.modify_time = create_time
        parent_directory.subDirectories.append(directory)
        directory.parentDirectory = parent_directory

    def open_directory(self, path):
        self.current_directory = path

    def delete_directory(self, directory):
        # 释放目录中的所有文件
        for file in directory.files:
            self.delete_file(file, directory)
        # 递归删除子目录
        for sub_dir in directory.subDirectories:
            self.delete_directory(sub_dir)
        # 从父目录的子目录列表中删除目录
        if directory.parentDirectory is not None:
            directory.parentDirectory.subDirectories.remove(directory)

        # 格式化
    def format(self):
        print("formatting..")
        self.root = Directory("root",None)
        self.current_directory=self.root
        self.blank = [i for i in range(1024)]
        self.physical_blocks = Block()

    def rename_file(self, fcb: FCB, new_name: str, parent_dir: Directory):
        fcb.name = new_name
        fcb.modify_time = datetime.now()
        parent_dir.modify_time = datetime.now()

    def rename_dir(self, dir: Directory, new_name):
        dir.name = new_name
        dir.modify_time = datetime.now() # 修改时间变更

    #保存到本地文件
    def save(self, system_info_file: str):
        # save all data in local position
        print("saving")
        with open(system_info_file, "wb") as f:
            #pickle.dump(self, f)
            pickle.dump(self.root, f)
            pickle.dump(self.current_directory, f)
            pickle.dump(self.physical_blocks, f)
            pickle.dump(self.blank, f)
            #pickle.dump(self.fat, f)

    def save_to_disk(self, filename):
        with open(filename, "wb") as file:
            pickle.dump(self, file)

    @staticmethod
    def load_from_disk(filename):
        with open(filename, "rb") as file:
            return pickle.load(file)

