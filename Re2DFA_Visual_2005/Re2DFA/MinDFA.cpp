/*****************************************************************
文件名：DFA_MIN.cpp
版本号：04
优化：
使用 Map类 来提供字符映射；
使用 Vector类 来解决自动机节点限制的问题；
使用 Hopcroft 算法进行自动机的化简，
将项目拆分成多个头文件
Author:Lijiyu
功能：将输入的正则表达式递归建立NFA，确定化为DFA，最终最小化DFA,使用Graphviz来辅助生成图像
支持空串，不存在空集合
时间：4.13
******************************************************************/
#pragma warning(disable:4996)


#include"Tools.h"
#include"FABuild.h"
#include"re2dfa_MainPage.h"

vector<Qnode>nfaNode;
vector<QGroup>dfaNode;
vector<QGroup>MdfaNode;
map<char, int>charMap;
set<char>CharSet;
int nReduce;
int nGroup;
int nNode;
bool fakeChar;

/* 
* Class:     MainPage
* Method:    getRowofNFA
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_re2dfa_MainPage_getRowofNFA
(JNIEnv *, jobject) {
	return (jint)nNode;
}
/*
* Class:     MainPage
* Method:    getRowofDFA
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_re2dfa_MainPage_getRowofDFA
(JNIEnv *, jobject) {
	return (jint)nGroup;
}
/*
* Class:     MainPage
* Method:    getRowofMDFA
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_re2dfa_MainPage_getRowofMDFA
(JNIEnv *, jobject) {
	return nReduce;
}
/*
* Class:     MainPage
* Method:    getEpsilon
* Signature: (I)[I
*/
JNIEXPORT jintArray JNICALL Java_re2dfa_MainPage_getEpsilon
(JNIEnv *env, jobject obj, jint index) {
	jintArray intArray = env->NewIntArray((jsize)nfaNode[index].epsilon.size());
	jint temp[500], i = 0;
	for (set<int>::iterator it = nfaNode[index].epsilon.begin(); it != nfaNode[index].epsilon.end(); ++it) {
		temp[i++] = *it;
	}
	env->SetIntArrayRegion(intArray, 0, (jsize)nfaNode[index].epsilon.size(), temp);
	return intArray;
}
/*
* Class:     MainPage
* Method:    getGroupofDFA
* Signature: (I)[I
*/
JNIEXPORT jintArray JNICALL Java_re2dfa_MainPage_getGroupofDFA
(JNIEnv *env, jobject obj, jint index) {
	jintArray intArray = env->NewIntArray((jsize)dfaNode[index].qns.size());
	jint temp[500], i = 0;//这里数组开小会爆炸！
	for (set<int>::iterator it = dfaNode[index].qns.begin(); it != dfaNode[index].qns.end(); ++it) {
		temp[i++] = *it;
	}
	env->SetIntArrayRegion(intArray, 0, (jsize)dfaNode[index].qns.size(), temp);
	return intArray;
}

/*
* Class:     MainPage
* Method:    getGroupofMDFA
* Signature: (I)[I
*/
JNIEXPORT jintArray JNICALL Java_re2dfa_MainPage_getGroupofMDFA
(JNIEnv *env, jobject obj, jint index) {
	jintArray intArray = env->NewIntArray((jsize)MdfaNode[index].qns.size());
	jint temp[500], i = 0;
	for (set<int>::iterator it = MdfaNode[index].qns.begin(); it != MdfaNode[index].qns.end(); ++it) {
		temp[i++] = *it;
	}
	env->SetIntArrayRegion(intArray, 0, (jsize)MdfaNode[index].qns.size(), temp);
	return intArray;
}
/*
* Class:     MainPage
* Method:    getNFA
* Signature: ()[[I
*/
JNIEXPORT jobjectArray JNICALL Java_re2dfa_MainPage_getNFA
(JNIEnv *env, jobject obj) {
	jclass intArrayClass = env->FindClass("[I");
	jobjectArray objectIntArray = env->NewObjectArray(nNode, intArrayClass, NULL);
	for (int i = 0; i < nNode; ++i) {
		jintArray intArray = env->NewIntArray((jsize)CharSet.size());
		jint temp[500]; int j = 0;
		for (set<char>::iterator it = CharSet.begin(); it != CharSet.end(); ++it) {
			temp[j++] = nfaNode[i].edge[edgeType(*it)];
		}
		env->SetIntArrayRegion(intArray, 0, (jsize)CharSet.size(), temp);
		env->SetObjectArrayElement(objectIntArray, i, intArray);
		env->DeleteLocalRef(intArray);
	}
	return objectIntArray;
}

/*
* Class:     MainPage
* Method:    getDFA
* Signature: ()[[I
*/
JNIEXPORT jobjectArray JNICALL Java_re2dfa_MainPage_getDFA
(JNIEnv *env, jobject obj) {
	jclass intArrayClass = env->FindClass("[I");
	jobjectArray objectIntArray = env->NewObjectArray(nGroup, intArrayClass, NULL);
	for (int i = 0; i < nGroup; ++i) {
		jintArray intArray = env->NewIntArray((jsize)CharSet.size());
		jint temp[500]; int j = 0;
		for (set<char>::iterator it = CharSet.begin(); it != CharSet.end(); ++it) {
			temp[j++] = dfaNode[i].edges[edgeType(*it)];
		}
		env->SetIntArrayRegion(intArray, 0, (jsize)CharSet.size(), temp);
		env->SetObjectArrayElement(objectIntArray, i, intArray);
		env->DeleteLocalRef(intArray);
	}
	return objectIntArray;
}
/*
* Class:     MainPage
* Method:    getMinDFA
* Signature: ()[[I
*/
JNIEXPORT jobjectArray JNICALL Java_re2dfa_MainPage_getMinDFA
(JNIEnv *env, jobject obj) {
	jclass intArrayClass = env->FindClass("[I");
	jobjectArray objectIntArray = env->NewObjectArray(nReduce, intArrayClass, NULL);
	for (int i = 0; i < nReduce; ++i) {
		jintArray intArray = env->NewIntArray((jsize)CharSet.size());
		jint temp[500]; int j = 0;
		for (set<char>::iterator it = CharSet.begin(); it != CharSet.end(); ++it) {
			temp[j++] = MdfaNode[i].edges[edgeType(*it)];
		}
		env->SetIntArrayRegion(intArray, 0, (jsize)CharSet.size(), temp);
		env->SetObjectArrayElement(objectIntArray, i, intArray);
		env->DeleteLocalRef(intArray);
	}
	return objectIntArray;
}
/*
* Class:     MainPage
* Method:    addCharSet
* Signature: (Ljava/lang/String;)V
*/
JNIEXPORT jint JNICALL Java_re2dfa_MainPage_addCharSet
(JNIEnv *env, jobject obj, jstring str) {
	CharSet.clear();
	int repeatFlag=0;
	const char*ChaTable = NULL;
	ChaTable = env->GetStringUTFChars(str, 0);
	int charCount = 0;
	for (int i = 0; ChaTable[i] != '\0'; ++i) {
		if (CharSet.count(ChaTable[i]) == 0)
			charMap[ChaTable[i]] = charCount++;
		else repeatFlag = 1;
		CharSet.insert(ChaTable[i]);
	}
	return repeatFlag;
}

JNIEXPORT jstring JNICALL Java_re2dfa_MainPage_GetCharSet
(JNIEnv *env, jobject) {
	int i=0;
	char AllChar[50];
	for (set<char>::iterator it = CharSet.begin(); it != CharSet.end(); it++)AllChar[i++] = *it;
	AllChar[i] = '\0';
	jstring rst = env->NewStringUTF(AllChar);
	return rst;
}
/*
* Class:     MainPage
* Method:    buildFA
* Signature: (Ljava/lang/String;)V
*/
JNIEXPORT jint JNICALL Java_re2dfa_MainPage_buildFA
(JNIEnv *env, jobject, jstring str) {
	nfaNode.clear();
	dfaNode.clear();
	MdfaNode.clear();
	CreatFolder();
	nNode = 2;
	nfaNode.push_back(Qnode(0));
	nfaNode.push_back(Qnode(1));
	nfaNode[1].finish = true;
	const char*Reg = env->GetStringUTFChars(str, 0);
	char Regular[100];
	fakeChar = false;
	strncpy(Regular, Reg, 100);
	int ParenthesisCount = 0;//辅助判断括号是否匹配
	for (int i = 0; Regular[i] != '\0'; ++i) {
		if (Regular[i] == '(') { 
			if (GRParenth(&Regular[i]) == NULL)return 2;
			++ParenthesisCount; 
		}
		else if (Regular[i] == ')')--ParenthesisCount;
	}
	if (ParenthesisCount != 0)return (jint)2;//括号不匹配（左右括号数量不相等）
	//建立NFA
	CreatNFA(0, 1, Regular);
	if (fakeChar == true)return (jint)1;//出现了不在字符集中的字符
	DrawEpsilon();
	system("dot -Tpng .\\graph\\epsilon_nfa.dot -o .\\graph\\NFA.png");
	system("dot -Tsvg .\\graph\\epsilon_nfa.dot -o .\\graph\\NFA.svg");
	//建立DFA
	ExpandEpsilon();
	BuildDFA();
	DrawDFA();
	system("dot -Tpng .\\graph\\dfa.dot -o .\\graph\\DFA.png");
	system("dot -Tsvg .\\graph\\dfa.dot -o .\\graph\\DFA.svg");
	//最小化DFA
	BuildMinDFA();
	DrawMinDFA();
	system("dot -Tpng .\\graph\\Reduced_DFA.dot -o .\\graph\\M_DFA.png");
	system("dot -Tsvg .\\graph\\Reduced_DFA.dot -o .\\graph\\M_DFA.svg");

	return (jint)0;
}

JNIEXPORT void JNICALL Java_re2dfa_MainPage_ShowPicture
(JNIEnv *, jobject) {
	Show();
	return;
}