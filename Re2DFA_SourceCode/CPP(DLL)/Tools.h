#pragma warning(disable:4996)
#ifndef _TOOLS_H__
#define _TOOLS_H__
#include<map>
#include<set>
#include<cstring>
#include<cstdio>
#include<vector>
#include<cstdlib>
using namespace std;

const int MaxEdge = 80;
//Node in NFA
struct Qnode {
	int edge[MaxEdge];
	set<int>epsilon;
	bool finish;
	Qnode() {};
	Qnode(int n) {
		epsilon.clear();
		epsilon.insert(n);
		finish = false;
		memset(edge, -1, sizeof(edge));
	}
};
//Node in DFA,used in DFA and Minimized DFA
struct QGroup {
	set<int>qns;
	int edges[MaxEdge];
	bool finish;
	QGroup() {
		qns.clear();
		memset(edges, -1, sizeof(edges));
		finish = false;
	}
};

extern vector<Qnode>nfaNode;
extern vector<QGroup>dfaNode;
extern vector<QGroup>MdfaNode;
extern map<char, int>charMap;
extern set<char>CharSet;
extern int nReduce;
extern int nGroup;
extern int nNode;
extern bool fakeChar;

int edgeType(char ch);
void AddEpsilon(int q1, int q2);
void AddEdge(int q1, int q2, char ch);
void ExpandEpsilon();
char *GRParenth(char *s);
char *FindOption(char *s);
bool subSetof(const set<int>&son, const set<int>&fath);
bool subSetofAll(const set<int>&son);
bool operator==(const QGroup &a, const QGroup &b);
void DrawDFA();
void DrawMinDFA();
void Show();
void DrawEpsilon();
void RunMinDFA();

//To provide a Map from char to int(index of char)
int edgeType(char ch) {
	if (CharSet.count(ch) == 0) {
		printf("Illegals Character in expression.\n");
		fakeChar = true;
		return 0;
	}
	return charMap[ch];
}

//������ɱ�
void AddEpsilon(int q1, int q2) {
	nfaNode[q1].epsilon.insert(q2);
}
//����ַ���
void AddEdge(int q1, int q2, char ch) {
	nfaNode[q1].edge[edgeType(ch)] = q2;
}
void ExpandEpsilon() {
	/*Function:Add the sub-sub-epsilon edge for current item.
	���ܣ���չ���ɱߣ�ͨ��������ɱߵ����ɱߣ�����������е�˫��ѭ����ʵ��*/
	for (int i = 0; i<nNode; ++i)
		for (int j = 0; j<nNode; ++j)
			for (set<int>::iterator it = nfaNode[j].epsilon.begin(); it != nfaNode[j].epsilon.end(); ++it)
				for (set<int>::iterator it1 = nfaNode[*it].epsilon.begin(); it1 != nfaNode[*it].epsilon.end(); ++it1)
					nfaNode[j].epsilon.insert((int)(*it1));

}
//���ƥ���������
char *GRParenth(char *s) 
{
	int pcount = 1;
	do {
		++s;
		if (*s == '(')
			++pcount;
		else if (*s == ')')
			--pcount;
	} while (pcount>0 && *s != '\0');
	if (*s == '\0') {
		printf("\n\t\tWrong Regular Expression: \n\n\t\tParenthesis don't match!\n\n");
		return NULL;
	}
	return s;
}//Match right parenthesis
char *FindOption(char *s) {
	while (*s != '\0') {
		if (*s == '|')
			return s;
		else if (*s == '(')
			s = GRParenth(s) + 1;
		else
			s++;
	}
	return NULL;//�����ƥ��ᵼ�¿մ���Ϊһ����
}//Find Matched '|'.

bool subSetof(const set<int>&son, const set<int>&fath) { //�ж��Ƿ���ĳһ�����͵��Ӽ�
	for (set<int>::iterator it = son.begin(); it != son.end(); ++it)
		if (fath.count(*it) == 0)
			return false;//һ�Բ��ϼ�Ϊ��
	return true;
}
bool subSetofAll(const set<int>&son) { //�ж��Ƿ�����֪�򻯼��͵��Ӽ�
	if (son.size() <= 1)
		return true;//��ʱ�е�Ԫ�ؼ����߿ռ���һ��������֪����
	if (son.count(-1) == 1)
		return false;/*son.size()>1; ������ͬʱ���ڿռ��ͷǿռ�,�������κ�һ������*/
	for (int i = 0; i <= nReduce; ++i)
		if (subSetof(son, MdfaNode[i].qns))
			return true;//һ����ͬ������
	return false;//ȫ��Ϊ��
}
bool operator==(const QGroup &a, const QGroup &b) { //�ж�����״̬�Ƿ�ȼۣ��������ս�״̬��ͬ����ÿһ���ߵ����춼��ͬ��������󻯼�����Ĳ�©��ȱ
	if (a.finish != b.finish)
		return false;
	set<int>desta;
	set<int>destb;
	for (set<char>::iterator ita = CharSet.begin(); ita != CharSet.end(); ++ita) {
		desta.clear();
		destb.clear();
		for (set<int>::iterator it = a.qns.begin(); it != a.qns.end(); ++it)
			desta.insert(dfaNode[*it].edges[edgeType(*ita)]);

		for (set<int>::iterator it = b.qns.begin(); it != b.qns.end(); ++it)
			destb.insert(dfaNode[*it].edges[edgeType(*ita)]);
		if (!(desta == destb))
			return false;
	}
	return true;
}
void DrawDFA()
{
	FILE*fp;
	if ((fp = fopen("./graph/dfa.dot", "w")) == NULL) {
		printf("Cannot Draw!\n");
		return;
	}
	fprintf(fp, "digraph DFA{\n\trankdir=LR;\nfontsize= 14;\n");
	fprintf(fp, "\tnode [shape = circle, fontname = \"Microsoft YaHei\", fontsize = 14];\n\tedge [fontname = \"Microsoft YaHei\", fontsize = 12];\n\n");

	for (int i = 0; i<nGroup; ++i) {
		if (dfaNode[i].finish == true)
			fprintf(fp, "\t%d [ shape = doublecircle ];\n", i);
	}
	fprintf(fp, "\t0 [color=red];\n\n");
	fprintf(fp, "\t\"\"[shape = \"none\"]\n\t\"\" -> 0;\n\n");
	for (int i = 0; i<nGroup; ++i) {
		for (set<char>::iterator it = CharSet.begin(); it != CharSet.end(); ++it) {
			int to = dfaNode[i].edges[edgeType(*it)];
			if (to != -1) {
				fprintf(fp, "\t%d -> %d [ label = \"%c\" ];\n", i, to, *it);
			}
		}
	}
	fprintf(fp, "\"DFA:\" [ shape = plaintext ];\n}\n");
	fclose(fp);
}
void DrawMinDFA() {
	/*Use Graphviz to visualize my NFA and DFAs.*/
	FILE*fp;
	if ((fp = fopen("./graph/Reduced_DFA.dot", "w")) == NULL) {
		printf("Cannot Draw!\n");
		return;
	}
	fprintf(fp, "digraph MinDFA{\n\trankdir=LR;\nfontsize= 14;\n");
	fprintf(fp, "\tnode [shape = circle, fontname = \"Microsoft YaHei\", fontsize = 14];\n\tedge [fontname = \"Microsoft YaHei\", fontsize = 12];\n\n");
	for (int i = 0; i<nReduce; ++i) {
		if (MdfaNode[i].finish == true)
			fprintf(fp, "\t%d [ shape = doublecircle ];\n", i);
	}
	fprintf(fp, "\t0 [color=red];\n");
	fprintf(fp, "\t\"\"[shape = \"none\"]\n\t\"\" -> 0;\n\n");
	for (int i = 0; i<nReduce; ++i) {
		for (set<char>::iterator it = CharSet.begin(); it != CharSet.end(); ++it) {
			int to = MdfaNode[i].edges[edgeType(*it)];
			if (to != -1) {
				fprintf(fp, "\t%d -> %d [ label = \"%c\" ];\n", i, to, *it);
			}
		}
	}
	fprintf(fp, "\"Min DFA:\" [ shape = plaintext ];\n}\n");
	fclose(fp);
}
void Show() {
	system(".\\graph\\NFA.svg");
	system(".\\graph\\DFA.svg");
	system(".\\graph\\M_DFA.svg");
}

void DrawEpsilon()
{
	FILE*fp;
	if ((fp = fopen("./graph/epsilon_nfa.dot", "w")) == NULL) {
		printf("Cannot Draw!\n");
		return;
	}
	fprintf(fp, "digraph epsilon{\n\trankdir=LR;\nfontsize= 14;\n");
	fprintf(fp, "\tnode [shape = circle, fontname = \"Microsoft YaHei\", fontsize = 14];\n\tedge [fontname = \"Microsoft YaHei\", fontsize = 12];\n\n");

	for (int i = 0; i<nNode; ++i) {
		if (nfaNode[i].finish == true)
			fprintf(fp, "\t%d [ shape = doublecircle ];\n", i);
	}
	fprintf(fp, "\t0 [color=red];\n");
	fprintf(fp, "\t\"\"[shape = \"none\"]\n\t\"\" -> 0;\n\n");
	for (int i = 0; i<nNode; ++i) {
		for (set<char>::iterator it = CharSet.begin(); it != CharSet.end(); ++it) {
			int to = nfaNode[i].edge[edgeType(*it)];
			if (to != -1) {
				fprintf(fp, "\t%d -> %d [ label = \"%c\" ];\n", i, to, *it);
			}
		}
		for (set<int>::iterator it = nfaNode[i].epsilon.begin(); it != nfaNode[i].epsilon.end(); ++it)
			if (*it != i)
				fprintf(fp, "\t%d -> %d [ style= \"dashed\" ];\n", i, *it);
	}
	fprintf(fp, "\"NFA:\" [ shape = plaintext ];\n}\n");
	fclose(fp);
}
void CreatFolder()
{
	FILE *fp;
	if ((fp = fopen(".\\graph\\temp.try", "w")) == NULL) {
		system("md .\\graph");
	}
	else {
		fclose(fp);
		system("del .\\graph\\temp.try");
	}
	return;
}
void RunMinDFA() {
	char expre[100], *p;
	printf("\nNow you can Enter some example sentence to test my Minimized DFA\n(characters should be included in the CharacterSet.)\n\n");
	while (fgets(expre, 100, stdin) != NULL) {
		p = expre;
		*strchr(expre, '\n') = '\0';
		int state = 0;/* 0�ǳ�ʼ״̬*/

		while (*p != '\0'&&state != -1)
			state = MdfaNode[state].edges[edgeType(*p++)];
		/*��������С����DFA��״̬ת��ֻ��Ҫһ�仰���������*/

		if (*p == '\0'&&state != -1 && MdfaNode[state].finish == true)//ʹ�ö�·�������������������ж��Ƿ��Զ�������
			printf("Able to accept.\n");
		else
			printf("Cannot accept.\n");
	}
	return;
}

#endif // _TOOLS_H__
