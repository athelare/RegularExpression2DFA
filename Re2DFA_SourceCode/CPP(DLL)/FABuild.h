#ifndef _FABUILD_H__
#define _FABUILD_H__
#include"Tools.h"
#pragma warning(disable:4996)
void CreatNFA(int qStart, int Next, char Reg[]);
void BuildDFA();
void BuildMinDFA();

void CreatNFA(int qStart, int Next, char Reg[]) {
	/*���������ν����Զ�����
	�մ���������Ϊ��epsilon��
	���л�����ı��ʽ
	�������Ӳ����ı��ʽ
	�����ſ�ʼ���ݹ�����Լ��������Զ�����ֱ����ǰ�������ţ�Ϊÿһ�����Ž���һ���Զ���
	����ַ�����ȡ��ǰ�ĵ�һ��Ԫ�صݹ���ñ�����������зֽ⣬ֱ�����ֽ�Ϊ�����ַ�
	�����ַ�������һ������һ�����ߵ��Զ���*/
	if (fakeChar)return;//�����˴����ַ�ֱ���˳���
	char *cur = Reg, *r; 
	char subReg[100];//��NFA

	//��Ӧ�ڿմ�
	if (*cur == '\0') {
		AddEpsilon(qStart, Next);
		return;
	}
	//�����ַ�
	if (Reg[1] == '\0') {
		AddEdge(qStart, Next, *Reg);
		return;
	}
	//��֧���,�ݹ齨����֧�ڵ��Զ���
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

	 //�ݹ齨������ֻ������Ҫ����Զ���
	int curFinal = nNode;//curFinal����NFA���ս�״̬
	nfaNode.push_back(Qnode(nNode++));
	if (*cur == '(') {
		r = GRParenth(cur);//��ƥ��ᱨ���˳�
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
	/*����NFA��ֱ��ģ���ֹ�����������ʼ״̬Ϊ��ʼ��Ŀ��ֱհ���
	Ȼ�����ÿһ���ַ����е��ַ���飬����û�������µ�״̬��
	�оͽ�����ӵ������С��Ա����������û�о�ֱ�����һ����ָ���Ѿ����ڵ�Ⱥ��*/
	//��ʼ����һ��DFA״̬
	nGroup = 1;
	dfaNode.push_back(QGroup());
	dfaNode[0].qns.insert(nfaNode[0].epsilon.begin(), nfaNode[0].epsilon.end());
	
	int n = 0;
	dfaNode.push_back(QGroup());
	while (n<nGroup) { //�ж��Ƿ񳬹��˵�ǰ��״̬������������µ�״̬nGroup���һ
		int des;
		bool flag;
		for (set<char>::iterator ita = CharSet.begin(); ita != CharSet.end(); ++ita) { //����ַ����м�飬Ҳ���Ǽ��ÿһ����
			dfaNode[nGroup] = QGroup();//�������
			flag = false;
			for (set<int>::iterator it = dfaNode[n].qns.begin(); it != dfaNode[n].qns.end(); ++it) { //���Ⱥ�����ÿһ�����Ƿ�����չ
				des = nfaNode[*it].edge[edgeType(*ita)];
				if (des == -1)
					continue;//����ڵ�ǰ�ַ�����������չ����ôֱ��Ѱ����һ���ַ���flag��־��¼�Ƿ���չ������һ��״̬
				flag = true;
				for (set<int>::iterator itd = nfaNode[des].epsilon.begin(); itd != nfaNode[des].epsilon.end(); ++itd)
					dfaNode[nGroup].qns.insert(*itd);//�����չ��ȥ֮��Ľڵ��epsilon�ߣ�����������״̬�Ѿ����ֹ�����ô��һ����Ὣ������
			}
			if (flag == true) { //���ͨ�����ַ��յ㲻Ϊ��
				int i;
				for (i = 0; i<nGroup; ++i) { //���ز���
					if (dfaNode[nGroup].qns == dfaNode[i].qns)
						break;//����ĵȺ����жϼ�����ȵ������
				}
				if (i == nGroup) { //���û�г��ֹ���ǰ����ϣ���ô�����³��ֵ�һ��״̬����ӵ�״̬������
					dfaNode[n].edges[edgeType(*ita)] = nGroup;
					dfaNode.push_back(QGroup());
					nGroup++;
				}//Notion:����ÿ��״̬��ÿ���ַ�ֻ���������һ����״̬
				else {
					dfaNode[n].edges[edgeType(*ita)] = i;//���ֹ����Ͳ���Ҫ�������,ֱ�����һ���߼���
				}
			}
		}
		n++;
	}
	//��ӽ�����־����Ϊ����NFA��ʱ���Ե�һ����Ϊ���յ�NFA����˰���1�Ķ�����̬
	for (int i = 0; i<nGroup; ++i)
		if (dfaNode[i].qns.count(1) == 1)
			dfaNode[i].finish = true;
}



void BuildMinDFA() {
	/**********************************
	ʹ��Hopcroft˼����л���
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
	/*����ʹ��Hopcroft˼����л������ʵ�ֽ����˼򻯣�
	����ַ��ܹ��з�ĳ�����ϣ��Ǿͽ�����Ϊ�������ϣ������Ƕ������*/
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
				//����һ��������ĵ�һ��Ԫ����Ϊ��׼������ͨ����ǰ�ַ������ֵ�һ���������Ԫ�ض���ӵ������һ���¼���
				++ite;
				if (~des) {
					for (n = 0; n<nReduce; ++n)
						if (MdfaNode[n].qns.count(des) == 1)break;

					for (; ite != MdfaNode[i].qns.end(); ++ite) {
						if (MdfaNode[n].qns.count(dfaNode[*ite].edges[edgeType(*ita)]) == 0)
							MdfaNode[nReduce].qns.insert(*ite);
					}
				}
				else {//���������ϵ�һ��Ԫ�ؾ�����ǰ�ַ�������ǿռ�����ô���ǿռ��Ķ�Ҫ������ȥ
					for (; ite != MdfaNode[i].qns.end(); ++ite) {
						if (dfaNode[*ite].edges[edgeType(*ita)] != -1)
							MdfaNode[nReduce].qns.insert(*ite);
					}
				}
				for (ite = MdfaNode[nReduce].qns.begin(); ite != MdfaNode[nReduce].qns.end(); ++ite)
					MdfaNode[i].qns.erase(*ite);
				MdfaNode.push_back(QGroup());//����һ����λ
				nReduce++;
			}
		}
		i++;
		if (ChangeFlag)i = 0;
		ChangeFlag = false;
	}
	//����ս�״̬���
	for (int i = 0; i<nReduce; ++i)
		for (set<int>::iterator ite = MdfaNode[i].qns.begin(); ite != MdfaNode[i].qns.end(); ++ite)
			if (dfaNode[*ite].finish == true) {
				MdfaNode[i].finish = true;
				break;
			}
	//����ЩȺ����ӵ�еĽڵ㼯�ıߵ���Ϣ��ӵ�Ⱥ�鱾��
	for (int i = 0; i<nReduce; ++i)
		for (set<char>::iterator ita = CharSet.begin(); ita != CharSet.end(); ++ita)
			for (set<int>::iterator ite = MdfaNode[i].qns.begin(); ite != MdfaNode[i].qns.end(); ++ite)  //���Ⱥ�����ÿһ�����Զ����ı��ܵ�������Ⱥ���ܵ�����
				if (dfaNode[*ite].edges[edgeType(*ita)] != -1) {
					int temp = dfaNode[*ite].edges[edgeType(*ita)];
					for (int j = 0; j<nReduce; ++j) { //����������ߵĿ��Ե���Щ�ڵ�
						if (MdfaNode[j].qns.count(temp) == 1) {
							MdfaNode[i].edges[edgeType(*ita)] = j;
							continue;//����һ�����е��п��ܺ������״̬�ļ��ϣ���Ϊһ��״̬���ܳ����ڶ��Ⱥ�����棩
						}
					}
				}
}

#endif // _FABUILD_H__

