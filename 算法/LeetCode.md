## 😎 [167. 两数之和 II - 输入有序数组](https://leetcode.cn/problems/two-sum-ii-input-array-is-sorted/)

### 双指针

```java
public int[] twoSum(int[] numbers, int target) {
    int left = 1;
    int right = numbers.length;
    while (left < right) {
        int sum = numbers[left - 1] + numbers[right - 1];
        if (sum == target) {
            return new int[]{left, right};
        } else if (sum < target) {
            left++;
        } else {
            right--;
        }
    }
    return null;
}
```







# Left

## 回文

### 😎[125. 验证回文串](https://leetcode.cn/problems/valid-palindrome/)

#### 双指针 + api

```java
public boolean isPalindrome(String s) {
    s = s.toLowerCase();
    char[] chs = s.toCharArray();
    int left = 0;
    int right = chs.length - 1;
    while (left < right) {
        char leftCh = chs[left];
        char rightCh = chs[right];
        if (!Character.isLetterOrDigit(leftCh)) {
            left++;
            continue;
        }
        if (!Character.isLetterOrDigit(rightCh)) {
            right--;
            continue;
        }
        if (leftCh != rightCh) {
            return false;
        }

        left++;
        right--;
    }
    return true;
}
```

#### 栈 + 队列 + 依次出

```java
public boolean isPalindrome(String s) {
    s = s.toLowerCase();
    char[] chs = s.toCharArray();
    LinkedList<Character> stack = new LinkedList<>();
    LinkedList<Character> queue = new LinkedList<>();
    for (char ch : chs) {
        if (Character.isLetterOrDigit(ch)) {
            stack.push(ch);
            queue.offer(ch);
        }
    }

    while (!stack.isEmpty()) {
        if (stack.pop() != queue.poll()) {
            return false;
        }
    }
    return true;
}
```



### 😎[9. 回文数](https://leetcode.cn/problems/palindrome-number/)

#### 转字符串 + 双指针

```java
public boolean isPalindrome(int x) {
    String s = String.valueOf(x);
    char[] chs = s.toCharArray();
    int left = 0;
    int right = chs.length - 1;
    while (left < right) {
        if (chs[left] != chs[right]) {
            return false;
        }
        left++;
        right--;
    }
    return true;
}
```

#### 栈 + 队列 + 依次出栈



## 😎[844. 比较含退格的字符串](https://leetcode.cn/problems/backspace-string-compare/)

### 栈

```java
public boolean backspaceCompare(String s, String t) {
    LinkedList<Character> first = getResult(s);
    LinkedList<Character> second = getResult(t);
    if (first.size() != second.size()) {
        return false;
    }

    while (!first.isEmpty()) {
        if (first.pop() != second.pop()) {
            return false;
        }
    }
    return true;
}

private LinkedList<Character> getResult(String str) {
    char[] chs = str.toCharArray();
    LinkedList<Character> stack = new LinkedList<>();
    for (int i = 0; i < chs.length; i++) {
        char ch = chs[i];
        if (ch != '#') {
            stack.push(ch);
        } else {
            if (!stack.isEmpty()) {
                stack.pop();
            }
        }
    }
    return stack;
}
```

## [1684. 统计一致字符串的数目](https://leetcode.cn/problems/count-the-number-of-consistent-strings/)

### 位运算+32数组

- 题目提示只包含小写字母的时候，就可以考虑使用位运算
- int一共4个字节，一共包含32位，因此可以用一个整数来表示其出现的数组
- 位运算，一个字符出现几次，并不能确定

```bash
# int整数表示小写字母的字符串: 重复字母，只统计一次
0000 0000 0000 0000 0000 0000 0000 0001

# allowed
    # 出现字母a:   mask<<(ch-'a')
0000 0000 0000 0000 0000 0000 0000 0001        

    # 出现字母c:   mask<<(ch-'a')
0000 0000 0000 0000 0000 0000 0000 0100       

   # 同时出现a和c： 将a和c的结果进行按位与
0000 0000 0000 0000 0000 0000 0000 0101 
```

```java
    public int countConsistentStrings(String allowed, String[] words) {
        int mask = 0;
        char[] allowedChs = allowed.toCharArray();
        for (char ch : allowedChs) {
            int c = 1 << ch - 'a'; // 当前处理的字符
            mask = mask | c;
        }

        int result = 0;
        for (String word : words) {
            char[] chs = word.toCharArray();
            int mask1 = 0;
            boolean flag = true;
            for (char ch : chs) {
                int c = 1 << ch - 'a'; // 每处理一个字符，就判断一下是否出现了allowed中没有的字符
                mask1 = mask1 | c;
                if ((mask1 | mask) != mask) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                result++;
            }
        }
        return result;
    }
```



### 26-int数组

```java
public int countConsistentStrings(String allowed, String[] words) {
    int[] arr = new int[26];
    char[] chs = allowed.toCharArray();
    for (char ch : chs) {
        arr[ch - 'a'] = 1;
    }

    int result = 0;
    for (String str : words) {
        boolean flag = true;
        char[] chars = str.toCharArray();
        for (char ch : chars) {
            if (arr[ch - 'a'] == 0) {
                flag = false;
                break;
            }
        }
        if (flag) {
            result++;
        }
    }
    return result;
}
```

### 26-boolean数组

- 效率更高

```java
public int countConsistentStrings(String allowed, String[] words) {
    boolean[] arr = new boolean[26];
    char[] chars = allowed.toCharArray();
    for (char ch : chars) {
        arr[ch - 'a'] = true;
    }

    int result = 0;
    for (String word : words) {
        boolean flag = true;
        char[] chs = word.toCharArray();
        for (char ch : chs) {
            if (!arr[ch - 'a']) {
                flag = false;
                break;
            }
        }
        if (flag) {
            result++;
        }
    }
    return result;
}
```

