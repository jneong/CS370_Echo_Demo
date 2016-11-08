These patterns are for use with the Intent Utterance Expander, found here:
https://lab.miguelmota.com/intent-utterance-expander/example/

Documentation for utterances can be found here:
https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/supported-phrases-to-begin-a-conversation


GetEventsOnDateIntent Utterances:
```
// THIS IS THE ONLY RULE THAT HAS BEEN USED SO FAR, THE OTHER RULES GENERATE TOO MANY UTTERANCES IN THE EXPANDER

    GetEventsOnDateIntent (give|tell) me a (|community|sports|diversity related|clubs|lecture|film) (|event|meeting) (|that is|that's) (|on|for) {date}
    
// THESE RULES ARE FOR THE OTHER FORMS PROVIDED IN THE AMAZON ALEXA SKILLS KIT - THE LINK TO THE DOCUMENTATION IS AT THE TOP OF THE FILE

    GetEventsOnDateIntent (give|tell) me me an (|alumni|athletics|intramural|organizations) (|event|meeting|) (|that is|that's) (|on|for) {date}
    GetEventsOnDateIntent (give|tell) me a (concert|show) (|that is|that's) (|on|for) {date}
    GetEventsOnDateIntent (give|tell) me (|what is|what's) (|on) {date} (that's|that is) (|for) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films) (|event) (|that is|that's) (|on|for) {date}
    GetEventsOnDateIntent (to|for) (locate|find) (|a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) (|event) (|that is|that's) (|happening|scheduled) (|on|for) {date} (|that is|that's) fun
    GetEventsOnDateIntent I (want|need) (a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) (|event) (|that is|that's) (|on|for) {date}
    GetEventsOnDateIntent (|what is|what's) (|on) {date} (|for) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) 

```

NextEventIntent Utterances:
```
// THIS IS THE RULE USED FOR THE NextEventIntent UTTERANCES

    NextEventIntent (give|tell) me the (|next|most recent) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) event
    
// THESE RULES ARE FOR THE OTHER FORMS PROVIDED IN THE AMAZON ALEXA SKILLS KIT - THE LINK TO THE DOCUMENTATION IS AT THE TOP OF THE FILE

    NextEventIntent (give|tell) me the (|next|most recent) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films) event (|that is|that's) next
    NextEventIntent (give|tell) me (|what is|what's) next (that's|that is) (|for) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films)
    NextEventIntent (give|tell|locate|find) me (|what is|what's) next (that's|that is) (|for) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films) (|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|on|for) {date} that (is fun|is interesting|cool)
    NextEventIntent is (|happening|scheduled) next (|for) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films)
    NextEventIntent (to|for) (locate|find) (a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) (|that is|that's) (|happening|scheduled) next
    NextEventIntent I (want|need) (a|an|the) next (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) (even|show|concert) 
    NextEventIntent (a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) event (|that is|that's) next
    NextEventIntent (|what is|what's) next (|for) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) (that is|that's) (fun|interesting|cool)  
    ```

```


NextEventIntent Utterances:
```
NextEventIntent (give|tell) me the (|next|most recent) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|on|for) {date} that (is fun|is interesting|cool)
NextEventIntent (give|tell) me (|what is|what's) (|on) {date} (that's|that is) (|for) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|on|for) {date} that (is fun|is interesting|cool)
NextEventIntent (give|tell|locate|find) me (|what is|what's) (|on) {date} (that's|that is) (|for) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|on|for) {date} that (is fun|is interesting|cool)
NextEventIntent (give|tell|locate|find) me (a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (|event|meeting|show|gathering|occasion|affair|concert|game)(|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|on|for) {date} that (|is) (fun|interesting|cool)  
NextEventIntent is (|happening|going|occuring|scheduled) (|on) {date} (that's|that is) (|for) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (is fun|is interesting|cool)
NextEventIntent (to|for) (locate|find) (|a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|happening|going|occuring|scheduled) (|on|for) {date} that (|is) (fun|interesting|cool)  
NextEventIntent I (want|need|require|request) (a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|on|for) {date}
NextEventIntent (a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|on|for) {date} that (|is) (fun|interesting|cool)  
NextEventIntent (|what is|what's) (|on) {date} (|for) (|community|alumni|sporting|athletics|intramural|diversity related|club|clubs|organization|organizations|lecture|lectures|film|films) (|event|meeting|show|gathering|occasion|affair|concert|game) that (|is) (fun|interesting|cool)  
```