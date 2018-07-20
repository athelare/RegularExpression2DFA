#ifndef _FABUILD_H__
#define _FABUILD_H__
#include"Tools.h"
#pragma warning(disable:4996)
void CreatNFA(int qStart, int Next, char Reg[]);
void BuildDFA();
void BuildMinDFA();

void CreatNFA(int qStart, int Next, char Reg[]) {
	/*对以下情形建立自动机：
	空串：将被认为是epsilon边
	带有或运算的表达式
	带有连接操作的表达式
	以括号开始：递归调用自己建立子自动机，直到当前不是括号，为每一个括号建立一个自动机
	多个字符：抽取当前的第一个元素递归调用本函数对其进行分解，直到被分解为单个字符
	单个字符：建立一个含有一条连线的自动机*/
	if (fakeChar)return;//出现了错误字符直接退出。
	char *cur = Reg, *r; 
	char subReg[100];//子NFA

	//对应于空串
	if (*cur == '\0') {
		AddEpsilon(qStart, Next);
		return;
	}
	//单个字符
	if (Reg[1] == '\0') {
		AddEdge(qStart, Next, *Reg);
		return;
	}
	//分支语句,递归建立分支内的自动机
	if ((r = FindOption(cur)) != NULL) {
		while ((r = FindOption(cur)) != NULL) {
			int curMid = nNode;
			nfaNode.push_back(Qnode(nNode++));
			int curFinal = nNode;
			nfaNode.push_back(Qnode(nNode++));
			strncpy(subReg, cur, r - cur);
			subReg[r - cur] = '\0';
			AddEpsilon(qStart, curMid);
			CreatNFA(curMid, curFinal, subReg);
			AddEpsilon(curFinal, Next);
			cur = r + 1;
		}

		int curMid = nNode;
		nfaNode.push_back(Qnode(nNode++));
		int curFinal = nNode;
		nfaNode.push_back(Qnode(nNode++));
		AddEpsilon(qStart, curMid);
		CreatNFA(curMid, curFinal, cur);
		AddEpsilon(curFinal, Next);
		return;
	}

	 //递归建立本层只有连接要求的自动机
	int curFinal = nNode;//curFinal是子NFA的终结状态
	nfaNode.push_back(Qnode(nNode++));
	if (*cur == '(') {
		r = GRParenth(cur);//不匹配会报错并退出
		strncpy(subReg, cur + 1, r - cur - 1);
		subReg[r - cur - 1] = '\0';
		++r;
	}
	else {
		subReg[0] = *cur;
		subReg[1] = '\0';
		r = cur + 1;
	}

	if (*r == '*') {
		int q1 = nNode;
		nfaNode.push_back(Qnode(nNode++));
		int q2 = nNode;
		nfaNode.push_back(Qnode(nNode++));
		//Thompson
		AddEpsilon(qStart, curFinal);
		AddEpsilon(qStart, q1);
		AddEpsilon(q2, curFinal);
		AddEpsilon(q2, q1);
		CreatNFA(q1, q2, subReg);
		++r;
	}
	else
		CreatNFA(qStart, curFinal, subReg);

	CreatNFA(curFinal, Next, r);
	return;
}

void BuildDFA() {
	/*建立NFA，直接模拟手工的做法，起始状态为起始点的克林闭包，
	然后对于每一个字符集中的字符检查，看有没有生成新的状态，
	有就将它添加到“队列”以便后续分析，没有就直接添加一条边指向已经存在的群体*/
	//初始化第一个DFA状态
	nGroup = 1;
	dfaNode.push_back(QGroup());
	dfaNode[0].qns.insert(nfaNode[0].epsilon.begin(), nfaNode[0].epsilon.end());
	
	int n = 0;
	dfaNode.push_back(QGroup());
	while (n<nGroup) { //判断是否超过了当前的状态数，如果出现新的状态nGroup会加一
		int des;
		bool flag;
		for (set<char>::iterator ita = CharSet.begin(); ita != CharSet.end(); ++ita) { //逐个字符进行检查，也就是检查每一条边
			dfaNode[nGroup] = QGroup();//清零操作
			flag = false;
			for (set<int>::iterator it = dfaNode[n].qns.begin(); it != dfaNode[n].qns.end(); ++it) { //检查群里面的每一条边是否能拓展
				des = nfaNode[*it].edge[edgeType(*ita)];
				if (des == -1)
					continue;//如果在当前字符不能向外拓展，那么直接寻找下一个字符，flag标志记录是否拓展出至少一个状态
				flag = true;
				for (set<int>::iterator itd = nfaNode[des].epsilon.begin(); itd != nfaNode[des].epsilon.end(); ++itd)
					dfaNode[nGroup].qns.insert(*itd);//添加拓展出去之后的节点的epsilon边，如果发现这个状态已经出现过，那么下一轮里会将它清零
			}
			if (flag == true) { //如果通过该字符终点不为空
				int i;
				for (i = 0; i<nGroup; ++i) { //查重操作
					if (dfaNode[nGroup].qns == dfaNode[i].qns)
						break;//这里的等号是判断集合相等的运算符
				}
				if (i == nGroup) { //如果没有出现过当前的组合，那么就是新出现的一个状态，添加到状态集里面
					dfaNode[n].edges[edgeType(*ita)] = nGroup;
					dfaNode.push_back(QGroup());
					nGroup++;
				}//Notion:对于每个状态的每个字符只会产生至多一个新状态
				else {
					dfaNode[n].edges[edgeType(*ita)] = i;//出现过，就不需要重新添加,直接添加一条边即可
				}
			}
		}
		n++;
	}
	//添加结束标志，因为建立NFA的时候以第一个作为最终的NFA，因此包含1的都是终态
	for (int i = 0; i<nGroup; ++i)
		if (dfaNode[i].qns.count(1) == 1)
			dfaNode[i].finish = true;
}



void BuildMinDFA() {
	/**********************************
	使用Hopcroft思想进行化简
	************************************/
	nReduce = 2;
	MdfaNode.push_back(QGroup());//Start State.
	MdfaNode.push_back(QGroup());//Finish State.
	MdfaNode[0].qns.insert(0);
	if (dfaNode[0].finish == true) {
		for (int i = 1; i<nGroup; ++i) {
			if (dfaNode[i].finish == true)
				MdfaNode[0].qns.insert(i);
			else
				MdfaNode[1].qns.insert(i);
		}
		if (MdfaNode[1].qns.size() == 0) {
			MdfaNode.pop_back();
			nReduce--;
		}
	}
	else {
		for (int i = 0; i<nGroup; ++i) {
			if (dfaNode[i].finish == true)
				MdfaNode[1].qns.insert(i);
			else
				MdfaNode[0].qns.insert(i);
		}
	}
	MdfaNode.push_back(QGroup());
	/*下面使用Hopcroft思想进行化简，针对实现进行了简化：
	如果字符能够切分某个集合，那就将它切为两个集合，而不是多个集合*/
	int n, i;
	set<int>destination;
	int ChangeFlag = false;
	i = 0;
	while (i<nReduce) {
		for (set<char>::iterator ita = CharSet.begin(); ita != CharSet.end() && !ChangeFlag; ++ita) {
			destination.clear();
			for (set<int>::iterator ite = MdfaNode[i].qns.begin(); ite != MdfaNode[i].qns.end() && !ChangeFlag; ++ite)
				destination.insert(dfaNode[*ite].edges[edgeType(*ita)]);

			if (subSetofAll(destination) == false) {
				ChangeFlag = true;
				set<int>::iterator ite = MdfaNode[i].qns.begin();
				int des = dfaNode[*ite].edges[edgeType(*ita)];
				//将这一个集合里的第一个元素作为基准，不能通过当前字符被划分到一个集合里的元素都添加到另外的一个新集合
				++ite;
				if (~des) {
					for (n = 0; n<nReduce; ++n)
						if (MdfaNode[n].qns.count(des) == 1)break;

					for (; ite != MdfaNode[i].qns.end(); ++ite) {
						if (MdfaNode[n].qns.count(dfaNode[*ite].edges[edgeType(*ita)]) == 0)
							MdfaNode[nReduce].qns.insert(*ite);
					}
				}
				else {//如果这个集合第一个元素经过当前字符到达的是空集，那么不是空集的都要被划出去
					for (; ite != MdfaNode[i].qns.end(); ++ite) {
						if (dfaNode[*ite].edges[edgeType(*ita)] != -1)
							MdfaNode[nReduce].qns.insert(*ite);
					}
				}
				for (ite = MdfaNode[nReduce].qns.begin(); ite != MdfaNode[nReduce].qns.end(); ++ite)
					MdfaNode[i].qns.erase(*ite);
				MdfaNode.push_back(QGroup());//保留一个空位
				nReduce++;
			}
		}
		i++;
		if (ChangeFlag)i = 0;
		ChangeFlag = false;
	}
	//添加终结状态标记
	for (int i = 0; i<nReduce; ++i)
		for (set<int>::iterator ite = MdfaNode[i].qns.begin(); ite != MdfaNode[i].qns.end(); ++ite)
			if (dfaNode[*ite].finish == true) {
				MdfaNode[i].finish = true;
				break;
			}
	//将这些群组所拥有的节点集的边的信息添加到群组本身
	for (int i = 0; i<nReduce; ++i)
		for (set<char>::iterator ita = CharSet.begin(); ita != CharSet.end(); ++ita)
			for (set<int>::iterator ite = MdfaNode[i].qns.begin(); ite != MdfaNode[i].qns.end(); ++ite)  //检查群里面的每一个子自动机的边能到哪里，这个群就能到哪里
				if (dfaNode[*ite].edges[edgeType(*ita)] != -1) {
					int temp = dfaNode[*ite].edges[edgeType(*ita)];
					for (int j = 0; j<nReduce; ++j) { //检查包含这个边的可以到哪些节点
						if (MdfaNode[j].qns.count(temp) == 1) {
							MdfaNode[i].edges[edgeType(*ita)] = j;
							continue;//遍历一边所有的有可能含有这个状态的集合（因为一个状态可能出现在多个群组里面）
						}
					}
				}
}

#endif // _FABUILD_H__

