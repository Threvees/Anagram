ANAGRAM PAIR APPLICATION by Ville Väisänen

Usable arguments for the program:

    1. anagram word for finding possible word pairings
    2. (true/false) Generate and print all permutations for chosen anagram word (WARNING: having a long word as anagram will cause massive string array generation or OutOfMemoryError!)
    3. URI for cross reference text file

Example: documenting false https://gist.com/wordlist.txt (Parameters 2 (false) and 3 (url) are optional)

Anagram pair application forms two-word anagrams of the given string if given word list contains two-word pairs that precisely form the given anagram word.

By default the word list URI is https://gist.githubusercontent.com/calvinmetcalf/084ab003b295ee70c8fc/raw/314abfdc74b50f45f3dbbfa169892eff08f940f2/wordlist.txt and the given anagram word is 'documenting'.

Anagram pair application also includes a permutation system, which you can use to view all the different forms of any given string. Permutation amount = n!, where n is the number of characters in given word