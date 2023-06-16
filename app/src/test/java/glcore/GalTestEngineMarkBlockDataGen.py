import random

import random

markStartPos = 3
markEndPos = markStartPos + random.randint(1, 100)
markBlockList = [f'MarkBlock({markStartPos}, {markEndPos}, "mark1")']
for i in range(2, 51):
    markStartPos = markEndPos + random.randint(0, 1000)
    markEndPos = markStartPos + random.randint(1, 100)
    markBlockList.append(f'MarkBlock({markStartPos}, {markEndPos}, "mark{i}")')

print("val markBlockTestDataList = listOf(")
print(",\n".join(markBlockList))
print(")")


testCases = []
for i in range(len(markBlockList)):
    markBlock = markBlockList[i]
    markStartPos = int(markBlock.split(",")[0].split("(")[1])
    markEndPos = int(markBlock.split(",")[1])
    testCases.append(f'Pair({markStartPos}, markBlockTestDataList[{i}])')
    testCases.append(f'Pair({markEndPos}, markBlockTestDataList[{i}])')
    testCases.append(f'Pair({(markStartPos + markEndPos) // 2}, markBlockTestDataList[{i}])')
    if i > 0:
        prevMarkEndPos = int(markBlockList[i - 1].split(",")[1])
        testCases.append(f'Pair({(prevMarkEndPos + markStartPos) // 2}, null)')
testCases.append('Pair(-1, null)')

print("val markBlockFromPosTestCaseList = listOf(")
print(",\n".join(testCases))
print(")")


testCases = []
for i in range(len(markBlockList)):
    markBlock = markBlockList[i]
    markStartPos = int(markBlock.split(",")[0].split("(")[1])
    markEndPos = int(markBlock.split(",")[1])
    if i > 0:
        prevMarkEndPos = int(markBlockList[i - 1].split(",")[1])
        testCases.append(f'Pair({(prevMarkEndPos + markStartPos) // 2}, markBlockTestDataList[{i - 1}])')
        testCases.append(f'Pair({markStartPos}, markBlockTestDataList[{i - 1}])')
        testCases.append(f'Pair({markEndPos}, markBlockTestDataList[{i - 1}])')
        testCases.append(f'Pair({markEndPos + 1}, markBlockTestDataList[{i}])')
    else:
        testCases.append(f'Pair({markStartPos - 1}, null)')
testCases.append('Pair(-1, null)')
testCases.append('Pair(9999999, markBlockTestDataList[markBlockTestDataList.size - 1])')

print("val prevMarkBlockOfPosTestCaseList = listOf(")
print(",\n".join(testCases))
print(")")


testCases = []
for i in range(len(markBlockList)):
    markBlock = markBlockList[i]
    markStartPos = int(markBlock.split(",")[0].split("(")[1])
    markEndPos = int(markBlock.split(",")[1])
    testCases.append(f'Pair({markStartPos - 1}, markBlockTestDataList[{i}])')
    if i < len(markBlockList) - 1:
        nextMarkStartPos = int(markBlockList[i + 1].split(",")[0].split("(")[1])
        testCases.append(f'Pair({(nextMarkStartPos + markEndPos) // 2}, markBlockTestDataList[{i + 1}])')
        testCases.append(f'Pair({markStartPos}, markBlockTestDataList[{i + 1}])')
        testCases.append(f'Pair({markEndPos}, markBlockTestDataList[{i + 1}])')
        testCases.append(f'Pair({markStartPos - 1}, markBlockTestDataList[{i}])')
    else:
        testCases.append(f'Pair({markEndPos}, null)')
testCases.append('Pair(-1, markBlockTestDataList[0])')
testCases.append('Pair(9999999, null)')

print("val nextMarkBlockOfPosTestCaseList = listOf(")
print(",\n".join(testCases))
print(")")

