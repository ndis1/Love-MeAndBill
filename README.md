Love-MeAndBill
==============

Git repo for my Love, Me and Bill Android app. 

Loss for Words? Shakespeare can help. The Bard finishes your sentences. This app uses unigram, 
bigram and trigram Markov models to generate text based on Shakespeare's works. 

This isn't a random text generator. Bill takes your thoughts and runs with them! Given what you wrote, 
what would Shakespeare say next?*

Begin your sentence, and when you're ready for Shakespeare to chime in, pass the quill over to him. Swipe
right for a suggestion from Bill, swipe down to use it. Swipe left to cycle through past suggestions, and
swipe up to cancel Shakespeare's input. 

Yours and Bill's joint works can be sent using your SMS or email client. 

There are 3 categories of source(Shakespeare) text:

Shakespeare the reveller - Comedy - the source texts are Shakespeare's plays:
    A Midsummer Night's Dream
    The Tempest
    The Merchant of Venice
    Much Ado About Nothing

Shakespeare the melancholic - Tragedy - the source texts are Shakespeare's plays :
    Hamlet
    Romeo + Juliet
    King Lear
    Othello 

Shakespeare the lover- Sonnets - the source texts are Shakespeare's sonnets 

source text taken from http://shakespeare.mit.edu/

*On unigram mode, text is totally random. On Bigram mode,
Shakespeare considers the last word you wrote, and on trigram mode,
the Bard considers the last two words you wrote. If Shakespeare never 
considered what what you typed in the relevant source text, the model falls back to more random text.
(Trigram->Bigram and Bigram->Unigram)
