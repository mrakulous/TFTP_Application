# Problems
- [ ] <b> 2. </b> Read, duplicate, RRQ print statement
- [ ] <b> 3. </b> Read, duplicate, DATA, needs to be handled properly by the client
- [ ] <b> 5. </b> Read, duplicate, ACK print statement
- [ ] <b> 6. </b> Should not be able to pick for example, WRQ when doing a read nor RRQ when writing
- [ ] <b> 7. </b> Invalid block number input is not handled

# Things We Need

- [x] Verbose/Quiet
- [x] Error/NoError
- [ ] <b> Server shutdown </b>
- [x] Allow more than one transfer
- [x] Empty file
- [x] Timeout

# Tests

<b> READ </b><br><br>

Normal <br>
- [x] Single block
- [x] Double block
- [x] 512 bytes
- [x] 132,000 bytes

Duplicate <br>
- [ ] <b> RRQ (Problem #2) </b>
- [ ] <b> Single block, DATA (Problem #3) </b>
- [ ] <b> Single block, ACK (Problem #5) </b>
- [ ] <b> Double block, DATA (Problem #3) </b>
- [ ] <b> Double block, ACK (Problem #5) </b>

Delay <br>
- [x] RRQ
- [x] Single block, DATA
- [x] Single block, ACK
- [x] Double block, DATA
- [x] Double block, ACK

Lost <br>
- [ ] <b> RRQ </b>
- [ ] <b> Single block, DATA </b>
- [ ] <b> Single block, ACK </b>
- [ ] <b> Double block, DATA </b>
- [ ] <b> Double block, ACK </b>

<hr>

<b> WRITE </b><br><br>

Normal <br>
- [x] Single block
- [x] Double block
- [ ] 512 bytes
- [ ] 132,000 bytes

Duplicate <br>
- [ ] WRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK

Delay <br>
- [ ] WRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK

Lost <br>
- [ ] WRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK
