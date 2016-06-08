# Things We Need
- [x] Verbose/Quiet
- [x] Error/NoError
- [ ] <b> Server shutdown </b>
- [x] Allow more than one transfer
- [x] Timeout

# Error 4 Cases
- [ ] Text and stuff

# Tests

<b> READ </b>
- [x] Empty file

Normal <br>
- [x] Single block
- [x] Double block
- [x] 512 bytes
- [x] 132,000 bytes

Duplicate <br>
- [x] RRQ
- [x] Single block, DATA
- [x] Single block, ACK
- [x] Double block, DATA, 1
- [x] Double block, ACK, 1

Delay <br>
- [x] RRQ
- [x] Single block, DATA
- [x] Single block, ACK
- [x] Double block, DATA, 1
- [x] Double block, ACK, 1

Lost <br>
- [ ] <b> RRQ </b>
- [ ] <b> Single block, DATA </b>
- [ ] <b> Single block, ACK </b>
- [ ] <b> Double block, DATA, 1 </b>
- [ ] <b> Double block, ACK, 1 </b>

<hr>

<b> WRITE </b>
- [x] Empty file

Normal <br>
- [x] Single block
- [x] Double block
- [x] 512 bytes 
- [x] 132,000 bytes

Duplicate <b>DOESNT SEND ACK BACK AT THE END</b><br>
- [x] WRQ
- [x] Single block, DATA
- [x] Single block, ACK
- [x] Double block, DATA, 1  VERY BROKEN
- [ ] <b> Double block, ACK, 1 </b>

Delay <br>
- [x] WRQ
- [x] Single block, DATA
- [x] Single block, ACK
- [x] Double block, DATA, 1
- [x] Double block, ACK, 1

Lost <br>
- [ ] <b> WRQ </b>
- [ ] <b> Single block, DATA </b>
- [ ] <b> Single block, ACK </b>
- [ ] <b> Double block, DATA, 1 </b>
- [ ] <b> Double block, ACK, 1 </b>
