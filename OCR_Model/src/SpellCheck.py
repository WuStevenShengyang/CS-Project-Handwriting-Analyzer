from spellchecker import SpellChecker

class SpellCheck:
    def __init__(self, words_arr):
        self.spell = SpellChecker()
        self.words_arr = words_arr
    
    def correction(self):
        self.misspelled = self.spell.unknown(self.words_arr)

        for word in self.misspelled:
            correct = self.spell.correction(word)
            try:
                self.words_arr[self.words_arr.index(word)] = correct
            except:
                self.words_arr[self.words_arr.index(word.capitalize())] = correct.capitalize()

        return self.words_arr