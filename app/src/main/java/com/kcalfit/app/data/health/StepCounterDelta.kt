package com.kcalfit.app.data.health

/** Never assigns a batch spanning local midnight or a sensor reboot to the wrong day. */
fun stepCounterDelta(previous:Long,current:Long,sameDay:Boolean):Long =
    if(sameDay && previous>=0 && current>=previous) current-previous else 0L
