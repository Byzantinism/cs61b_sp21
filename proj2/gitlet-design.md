# Gitlet Design Document

**Name**: Zebang Ge
## Design Steps
0. 大致划分Gitlet结构
1. 分解需求+填充到各个类
2. 根据需求确认所需数据类型
3. 细分需求到类内的函数
4. 优先完成I/O 函数和Helper function (装饰器)
5. 其他函数

### 框架描述
1. 由main导入参数, 确认情况后调用 全Static的Repository 的内部函数.
2. 各功能需要独立的独立, 可复印其他模块的就复用.

### .git结构
logs/<br>
   HEAD(String)<br>
   main(String)?????<br>
objects/<br>
   Staged(序列化)<br>
   Removed(序列化)<br>
   TreeMap<String, LinkedList<String>> branches **利用Java序列化的特性来使用branches矩阵储存**<br>
   HashMap+blob原文件+改文件名<br>
   HashMap+commit序列化<br>
refs/<br>
   HEAD(String)<br>
   String[] branches(序列化)<br>

## Implementation steps
1. I/O
2. commit
3. checkout
4. 待定

### 单个函数实现顺序
1. Corner case 和 Unit test
2. 函数内容
3. debug

## Utils可用函数
static String sha1(Object... vals): 合并产生SHA-1<br>
static String sha1(List<Object> vals): 合并产生SHA-1<br>

static boolean restrictedDelete(File file):删除file(1.不是路径的话,2. 内部没有.gitlet文件夹)<br>
static boolean restrictedDelete(String file): 处理字符路径<br>

static byte[] readContents(File file): 读取文件为二级制矩阵()<br>
static String **readContentsAsString**(File file): 读取文件为字符(调用上面这个)<br>
static <T extends Serializable> T **readObject**(File file, Class<T> expectedClass): 从序列化的文件中读取T类的实例<br>

static void **writeContents**(File file, Object... contents): 写入文件(Byte or String)<br>
static void **writeObject**(File file, Serializable obj): 序列化写入(调用上面这个)<br>

static List<String> plainFilenamesIn(File dir): 返回所有文件名<br>
static List<String> plainFilenamesIn(String dir): 同上, 处理字符路径<br>

static File join(String first, String... others): 合并路径<br>
static File join(File first, String... others): 合并路径<br>

static byte[] serialize(Serializable obj): 返回序列化的obj<br>

static GitletException error(String msg, Object... args): 报错<br>
static void message(String msg, Object... args): 打印报错<br>

### I/O 函数
1. implements Serializable
2. If you find yourself using readers, writers, scanners, or streams, you’re making things more complicated than need be.
3. Be careful. Methods such as File.list and File.listFiles produce file names in an undefined order. If you use them to implement the log command, in particular, you can get random results.
4. Windows users especially should beware that the file separator character is / on Unix (or MacOS) and ‘\’ on Windows. So if you form file names in your program by concatenating some directory names and a file name together with explicit /s or \s, you can be sure that it won’t work on one system or the other. Java provides a system-dependent file separator character (System.getProperty("file.separator")), or you can use the multi-argument constructors to File.
5. Be careful using a HashMap when serializing! The order of things within the HashMap is non-deterministic. The solution is to use a TreeMap which will always have the same order. 


寻找特定commit和blob<br>
使用TreeMap检索SHA-1值-文件路径, 在硬盘中实际存储按照HashMap来<br>
传递进来的参数: SHA-1值<br>
缓存里面的Map: Tree Map<br>
中间参数: 具体文件地址<br>
硬盘的文件Map结构(HashMap):theta(1) * file size<br>
**存储时修改文件名为对应的SHA-1的后38位!**<br>

**在前面加入字符来区分Commit和Blob, 并且在代码里面核验(采取2个文件夹的方式来区分Commit和Blob)**
#### Method
先确定文件夹结构 
1. 写入和读取序列化文件
2. 读写字符
3. 读取+修改+写入

#### Commit
没有Commit的SHA-1值-文件路径的map, 直接使用SHA-1值生成文件路径, **有全Commit的graph**.
#### Blob
在Commit的内部建立tries来容纳Blob的检索. 
1.**如何存储Blob文件? 并且恢复文件名?**:  
把文件直接移动过来,然后改名成SHA-1值,原文件名储存Blob TreeMap里面[Key = 原文件名, value = SHA-1]


### 序列化的指针问题 (Serialization Details 需要细看)
1. To avoid this, don’t use Java pointers to refer to commits and blobs in your runtime objects, but instead use SHA-1 hash strings. Maintain a runtime map between these strings and the runtime objects they refer to. You create and fill in this map while Gitlet is running, but never read or write it to a file.  
   把指向Commit和Blob的指针用SHA-1值来表示,在运行时建立一个SHA-1对应于指向对象的一个map(此map在runtime时建立)<br>  

   You might find it convenient to have (redundant) pointers commits as well as SHA-1 strings to avoid the bother and execution time required to look them up each time. You can store such pointers in your objects while still avoiding having them written out by declaring them “transient”, as in
   **private transient MyCommitType parent1;**<br>  
   额外构建一个单独的pointer包含SHA1和指向对应的commit和parent?<br>  
Such fields will not be serialized, and when back in and deserialized, will be set to their default values (**null for reference types**). You must be careful when reading the objects that contain transient fields back in to set the transient fields to appropriate values.
2. Unfortunately, looking at the serialized files your program has produced with a text editor (for debugging purposes) would be rather unrevealing; the contents are encoded in Java’s private serialization encoding. We have therefore provided a simple debugging utility program you might find useful: **gitlet.DumpObj**. See the Javadoc comment on gitlet/DumpObj.java for details.  
   反序列化之后,可以查看被序列化的内容.

### Runtime measure standard
1. The significant measures are: any measure of number or size of files, any measure of number of commits. 
2. You can ignore time required to serialize or deserialize, with the one caveat that your serialization time cannot depend in any way on the total size of files that have been added, committed, etc 
3. You can also pretend that getting from a hash table is constant time.

### Error
All error message end with a **period**.   
5. the status command printing the Modifications Not Staged For Commit and Untracked Files sections

6. Do **NOT** print out anything except for what the spec says. Some of our autograder tests will break if you print anything more than necessary.
7. To exit your program immediately, you may call **System.exit(0)**


#### SHA-1
2. **SHA-1.** Every object–every blob and every commit in our case–has a unique integer id that serves as a reference to the object
3. **SHA-1.** **Distinguishing** somehow between hashes for commits and hashes for blobs. A good way of doing this involves a well-thought out directory structure within the .gitlet directory. Another way to do so is to hash in an extra word for each object that has one value for blobs and another for commits.
**有些报错需要查看是不是真的有这个SHA-1值的. 把Blob和Commit分成2个文件夹存放, 跑的时候通过文件结果把HashMap组出来**
## Classes and Data Structures

### Class Main
#### Fields

1. Field 1
2. Field 2
#### Failure case and corner case
There are some failure cases you need to handle that don’t apply to a particular command. Here they are:
1. If a user doesn’t input any arguments, print the message **Please enter a command.** and exit.
2. If a user inputs a command that doesn’t exist, print the message **No command with that name exists.** and exit.
3. If a user inputs a command with the wrong number or format of operands, print the message **Incorrect operands.** and exit.
4. If a user inputs a command that requires being in an initialized Gitlet working directory, but is not in such a directory, print the message **Not in an initialized Gitlet directory.**

## Class I/O
1. 
2. 
### Class Repository
建立commit的map: key = commit的SHA-1值, value = commit的File address.<br>
建立branch的map, branch[0] = master: key = branch name, value = 指向的commit的SHA-1值<br>
给branch单独建立磁盘文件存储对应commit的SHA-1值<br>
#### Fields
1. CWD
2. GITLET_DIR
3. File Head: 从branch[]获取对应的file.
"detached HEAD" state——HEAD头指针指向了一个具体的提交ID，而不是一个分支. 正常情况下,HEAD指向一个branch,而branch又指向一个commit。 detached HEAD state指的是HEAD指针没有指向任何的branch,比如说它指向了一个commit。
4. File[] branch: branches[0] = master的路径
5. TreeMap Staged: key = 文件名, value = SHA-1
6. TreeMap Removed: key = 文件名, value = SHA-1
7. TreeMap Modifications Not Staged For Commit: key = 文件名, value = SHA-1 (每次调用status的时候检查?)
8. TreeMap Untracked: key = 文件名, value = SHA-1 (每次调用status的时候检查?)
9. dateFormat

### Method
1. init: 调用Commit. Runtime: Constant. 建立一个特殊的commit.
#### 2. add 
Runtime: In the worst case, should run in linear time relative to the size of the file being added and lgN, for N the number of files in the commit.<br>
塞进 Staging area然后将其序列化<br>
主要调用I/O函数<br>

private add(File){}<br>
Repository.[TreeMap Staged: key = 文件名, value = SHA-1]

1. Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
2. If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back to it’s original version).
3. The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.

#### 其他
3. Commit: **见下**
4. rm: 不涉及数据结构 
5. log: 逆序列化Commit, 然后调用Commit.print,找到p1接着递归
6. global-log: 调用private log 
7. find: 历遍所有
8. status: 需要调用Repository中的static 矩阵
9. checkout: checkout的时候只变化需要变化的文件.  
**?????** _EDITED 3/5: Note that in Gitlet, there is no way to be in a detached head state since there is no checkout command that will move the HEAD pointer to a specific commit. The reset command will do that, though it also moves the branch pointer. Thus, in Gitlet, you will never be in a detached HEAD state._
10. branch: 在branch[]中增加一个pointer.
11. rm-branch: 删除对应branch pointer 
12. reset: checkout到某Commit上, 并且移动branch pointer和Head pointer. **1个branch有1个linkedlist, 可以通过popout来拿掉之前commit但是回头要merge的话,就不好处理了. 还是1个commit1个linkedlist顺带塞进SHA1/date/message**
13. merge: 

### Class Commit
1. Commit trees are immutable: once a commit node has been created, it can never be destroyed (or changed at all)
2. Having our metadata consist only of a timestamp and log message. A commit, therefore, will consist of a log message, timestamp, a mapping of file names to blob references, a parent reference, and (for merges) a second parent reference. 
3. Incorporating trees into commits and not dealing with subdirectories **(so there will be one “flat” directory of plain files for each repository)**. 
4. Limiting ourselves to merges that reference **two** parents (in real Git, there can be any number of parents.)

Runtime: constant
从Head出获取当前commit,从硬盘中反序列化,然后复制一个对其进行修改, 抓取staging area里面文件, commit之后移除staging area的属性
#### Fields
1. String message
2. Date currentTime
3. Commit p1 + p2

#### Method
1. public Commit (String message, Date currentTime, Commit p1, Commit p2)
2. public print<br>
   '===<br>
   commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48<br>
   Date: Thu Nov 9 20:00:05 2017 -0800<br>
   A commit message.<br>
   空行<br>

3. getParent(int parent number)
### Class Merge
1. 先分析Split point, 然后比对Commit1和Commit2相对和Split point的不同,保留最新的改动.有冲突的就是conflict, 合到一起提醒手动修改.
2. 以当前Head为基准, 调用private add(File)/rm和commit, 形成新的Commit. (放入p1和p2).

#### Fields
1. Split point
2. p1
3. p2


### Checkout
1. java gitlet.Main checkout -- [file name]: **调用2**
2. java gitlet.Main checkout [commit id] -- [file name]
3. java gitlet.Main checkout [branch name]

#### Runtimes
1. Should be linear relative to the size of the file being checked out.
2. Should be linear with respect to the total size of the files in the commit’s snapshot. Should be constant with respect to any measure involving number of commits. Should be constant with respect to the number of branches. <br> 
**直接使用提供的commit的SHA-1值来确定路径**

## Algorithms

## Persistence

