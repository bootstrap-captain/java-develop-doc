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



## [1. 两数之和](https://leetcode.cn/problems/two-sum/)

### Hash

```java
public int[] twoSum(int[] nums, int target) {
    int[] arr = new int[2];
    Map<Integer, Integer> hash = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        if (hash.containsKey(target - nums[i])) {
            arr[0] = i;
            arr[1] = hash.get(target - nums[i]);
            return arr;
        }

        hash.put(nums[i], i);
    }
    return arr;
}
```

## [268. 丢失的数字](https://leetcode.cn/problems/missing-number/)

### Hash

```java
public int missingNumber(int[] nums) {
    Set<Integer> hash = new HashSet<>();
    for (Integer num : nums) {
        hash.add(num);
    }
    // 从0-n检查
    for (int i = 0; i <= nums.length; i++) {
        if (!hash.contains(i)) {
            return i;
        }
    }
    return -1;
}
```

### 总数和

```java
/*  n个元素的题目要求的总数和：
 [0]:   1
 [0,1]: 3
 [0,1]的是3 [0,1,2]=6*/
public int missingNumber(int[] nums) {
    int sum = nums.length * (nums.length + 1) / 2;
    for (int num : nums) {
        sum = sum - num;
    }
    return sum;
}
```

## [136. 只出现一次的数字](https://leetcode.cn/problems/single-number/)

### Hash

```java
public int singleNumber(int[] nums) {
    Map<Integer, Integer> hash = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        if (hash.containsKey(nums[i])) {
            hash.put(nums[i], -1); // 重复则置为-1
        } else {
            hash.put(nums[i], 1);
        }
    }

    for (Map.Entry<Integer, Integer> entry: hash.entrySet()){
        if (entry.getValue()==1){
            return entry.getKey();
        }
    }

    return 0;
}
```

### 按位异或

- 任何数字和其本身异或，结果为0
- 任何数字和0异或，结果为其本身
- 和顺序无关

```java
public int singleNumber(int[] nums) {
    int result = 0;
    for (int num : nums) {
        result = result ^ num;
    }
    return result;
}
```



## [88. 合并两个有序数组](https://leetcode.cn/problems/merge-sorted-array/)

### 逆向双指针

- 逆序从大到小
- 谁大就放在数组1的最右边

```java
public void merge(int[] nums1, int m, int[] nums2, int n) {
    int p = nums1.length - 1;
    int p1 = m - 1;
    int p2 = n - 1;
    while (p1 >= 0 && p2 >= 0) {
        if (nums1[p1] < nums2[p2]) {
            nums1[p] = nums2[p2];
            p2--;
        } else {
            nums1[p] = nums1[p1];
            p1--;
        }
        p--;
    }
    /*数组2不为空：复制
     * 数组1不为空：不用做*/
    if (p1 < 0) {
        for (int i = 0; i < p2 + 1; i++) {
            nums1[i] = nums2[i];
        }
    }
}
```

## [3. 无重复字符的最长子串](https://leetcode.cn/problems/longest-substring-without-repeating-characters/)

###  Hash去重+删除

```java
public int lengthOfLongestSubstring(String s) {
    int max = 0;
    char[] chars = s.toCharArray();
    Map<Character, Integer> hash = new HashMap<>();
    for (int i = 0; i < chars.length; i++) {
        if (hash.containsKey(chars[i])) {
            emptyBefore(hash, hash.get(chars[i])); // 清空之前的数据
        }
        hash.put(chars[i], i);
        max = Math.max(max, hash.size()); // 每次添加一个都更新一次
    }
    return max;
}

private void emptyBefore(Map<Character, Integer> hash, int index) {
    Iterator<Map.Entry<Character, Integer>> iterator = hash.entrySet().iterator();
    while (iterator.hasNext()) {
        Map.Entry<Character, Integer> entry = iterator.next();
        if (entry.getValue() <= index) {
            iterator.remove();
        }
    }
}
```

### Hash去重+有效标记

- 效率相比删除更高

```java
public int lengthOfLongestSubstring(String s) {
    int max = 0;
    int startPoint = 0;
    char[] chars = s.toCharArray();
    Map<Character, Integer> hash = new HashMap<>();

    for (int i = 0; i < chars.length; i++) {
        if (hash.containsKey(chars[i]) && hash.get(chars[i]) >= startPoint) {  // abba
            Integer index = hash.get(chars[i]);
            startPoint = index + 1;
        }
        hash.put(chars[i], i);
        max = Math.max(max, (i - startPoint + 1));
    }

    return max;
}
```

## [242. 有效的字母异位词](https://leetcode.cn/problems/valid-anagram/)

- 一般先将字符转换为数组，不用使用 s.charAt(i)
- 遇见字母都是小写，可以考虑长度为26的Charactor数组

### 字母重排

```java
public boolean isAnagram(String s, String t) {
    char[] ch1 = s.toCharArray();
    char[] ch2 = t.toCharArray();
    Arrays.sort(ch1);
    Arrays.sort(ch2);
    return new String(ch1).equals(new String(ch2));
}
```

### 26数组

```java
public boolean isAnagram(String s, String t) {
    int[] first = get(s);
    int[] second = get(t);
    for (int i = 0; i < first.length; i++) {
        if (first[i] != second[i]) {
            return false;
        }
    }
    return true;
}

private int[] get(String str) {
    int[] arr = new int[26];
    char[] chars = str.toCharArray();
    for (int i = 0; i < chars.length; i++) {
        int ch = chars[i]; // 转int
        arr[ch - 97]++;    // 97
    }
    return arr;
}
```

## [49. 字母异位词分组](https://leetcode.cn/problems/group-anagrams/)

### Hash+排序数组

```java
public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> hash = new HashMap<>();
    for (int i = 0; i < strs.length; i++) {
        String str = strs[i];
        char[] chs = str.toCharArray();
        Arrays.sort(chs);
        String newStr = new String(chs);
        if (hash.containsKey(newStr)) {
            hash.get(newStr).add(str);
        } else {
            ArrayList<String> sub = new ArrayList<>();
            sub.add(str);
            hash.put(newStr, sub);
        }
    }
    return new ArrayList<>(hash.values());
}
```

### Hash+26数组

```java
class PArray {
    int[] arr = new int[26];

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PArray pArray = (PArray) o;
        return Arrays.equals(arr, pArray.arr);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arr);
    }

    public void convertStrToArray(String str) {
        char[] chs = str.toCharArray();
        for (int i = 0; i < chs.length; i++) {
            int ch = chs[i];
            arr[ch - 97]++;
        }
    }
}

public List<List<String>> groupAnagrams(String[] strs) {
    Map<PArray, List<String>> hash = new HashMap<>();
    for (int i = 0; i < strs.length; i++) {
        String str = strs[i];
        PArray pArray = new PArray();
        pArray.convertStrToArray(str);

        if (hash.containsKey(pArray)) {
            hash.get(pArray).add(str);
        } else {
            ArrayList<String> sub = new ArrayList<>();
            sub.add(str);
            hash.put(pArray, sub);
        }
    }
    return new ArrayList<>(hash.values());
}
```

## [349. 两个数组的交集](https://leetcode.cn/problems/intersection-of-two-arrays/)

### Hash

```java
public int[] intersection(int[] nums1, int[] nums2) {
    Set<Integer> result = new HashSet<>();
    // 数组1去重
    Set<Integer> set1 = new HashSet<>();
    for (int num : nums1) {
        set1.add(num);
    }

    // 数组2判断: 添加结果集中，去重
    for (int num : nums2) {
        if (set1.contains(num)) {
            result.add(num);
        }
    }

    int[] arr = new int[result.size()];
    int i = 0;
    for (Integer el : result) {
        arr[i] = el;
        i++;
    }
    return arr;
}
```

## [387. 字符串中的第一个唯一字符](https://leetcode.cn/problems/first-unique-character-in-a-string/)

### Hash

-  重复元素的value为-1
- 第一个元素，通过遍历字符串得到

```java
public int firstUniqChar(String s) {
    Map<Character, Integer> hash = new HashMap<>();
    char[] chs = s.toCharArray();
    for (int i = 0; i < chs.length; i++) {
        if (hash.containsKey(chs[i])) {
            hash.put(chs[i], -1);
        } else {
            hash.put(chs[i], i);
        }
    }

    for (int i = 0; i < chs.length; i++) {
        int value = hash.get(chs[i]);
        if (value != -1) {
            return value;
        }
    }
    return -1;
}
```

### 26数组

- 可以通过计算'a'的方式

```java
public int firstUniqChar(String s) {
    int[] arr = new int[26];
    char[] chs = s.toCharArray();
    for (int i = 0; i < chs.length; i++) {
        arr[chs[i] - 'a']++;
    }

    for (int i = 0; i < chs.length; i++) {
        if (arr[chs[i] - 'a'] == 1) {
            return i;
        }
    }
    return -1;
}
```

## [217. 存在重复元素](https://leetcode.cn/problems/contains-duplicate/)

### Hash

```java
public boolean containsDuplicate(int[] nums) {
    Set<Integer> hash = new HashSet<>();
    for (Integer el : nums) {
        if (!hash.add(el)) {
            return true;
        }
    }
    return false;
}
```

## [219. 存在重复元素 II](https://leetcode.cn/problems/contains-duplicate-ii/)

```java
public boolean containsNearbyDuplicate(int[] nums, int k) {
    Map<Integer, Integer> hash = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        if (hash.containsKey(nums[i])) {
            if (i - hash.get(nums[i]) <= k) {
                return true;
            }
        }
        hash.put(nums[i], i);
    }
    return false;
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

