These patterns are for use with the Intent Utterance Expander, found here:
https://lab.miguelmota.com/intent-utterance-expander/example/

Documentation for utterances can be found here:
https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/supported-phrases-to-begin-a-conversation


GetEventsOnDateIntent Utterances:
```
GetEventsOnDateIntent (give|tell) me (what is|what's|what's up) (scheduled|happening) (for|on) the (events|s.s.u.|(sonoma state|sonoma state university))) calendar (for|on) {date} 
GetEventsOnDateIntent (give|tell) me (a|an|the) event (that is|that's) on {date}
GetEventsOnDateIntent (give|tell) me (what is|what's|what's up) on {date}
GetEventsOnDateIntent I (want|need) an (event|occasion) on {date} 
GetEventsOnDateIntent I (want|need) a (gathering|fun time) on {date}
GetEventsOnDateIntent (what is|what's) on {date} (that is|that's) (fun|interesting|cool)
GetEventsOnDateIntent (what is|what's) on {date} for (fun|me to do|an activity)
   
// THESE RULES ARE FOR THE OTHER FORMS PROVIDED IN THE AMAZON ALEXA SKILLS KIT USE AT YOUR DISCRETION - THE LINK TO THE DOCUMENTATION IS AT THE TOP OF THE FILE

```

NextEventIntent Utterances:
```
// THIS IS THE RULE USED FOR THE NextEventIntent UTTERANCES

    NextEventIntent (give|tell|provide) me the  (|next|most recent|upcoming|newest|latest|) (event|show|thing|shindig|meeting|occasion|presenation|) 
    
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

GetFeeDetailsIntent Utterances:
```
// THIS IS THE RULE USED FOR THE GetFeeDetailsIntent UTTERANCES

    GetFeeDetailsIntent (give|tell|provide) me the  (price|cost|amount|fee|charge) for the (event|show|thing|shindig|meeting|occasion|presentation|) {eventName}
    GetFeeDetailsIntent how much (does it cost|will I be charged|will I need to pay|will it cost|will I need) for the (event|show|thing|shindig|meeting|occasion|presentation|) {eventName} 
    
// THESE RULES ARE FOR THE OTHER FORMS PROVIDED IN THE AMAZON ALEXA SKILLS KIT - THE LINK TO THE DOCUMENTATION IS AT THE TOP OF THE FILE

    GetFeeDetailsIntent (give|tell) me the (|next|most recent) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films) event (|that is|that's) next
    GetFeeDetailsIntent (give|tell) me (|what is|what's) next (that's|that is) (|for) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films)
    GetFeeDetailsIntent (give|tell|locate|find) me (|what is|what's) next (that's|that is) (|for) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films) (|event|meeting|show|gathering|occasion|affair|concert|game) (|that is|that's) (|on|for) {date} that (is fun|is interesting|cool)
    GetFeeDetailsIntent is (|happening|scheduled) next (|for) (|community|alumni|sporting|athletics|intramural|diversity related|clubs|organizations|lectures|films)
    GetFeeDetailsIntent (to|for) (locate|find) (a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) (|that is|that's) (|happening|scheduled) next
    GetFeeDetailsIntent I (want|need) (a|an|the) next (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) (even|show|concert) 
    GetFeeDetailsIntent (a|an) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) event (|that is|that's) next
    GetFeeDetailsIntent (|what is|what's) next (|for) (|community|alumni|sporting|athletics|intramural|diversity related|club|organizations|lectures|films) (that is|that's) (fun|interesting|cool)  
    
```