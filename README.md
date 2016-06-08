# Things We Need

- [ ] Verbose/Quiet
- [ ] Error/NoError
- [ ] Server shutdown
- [ ] Allow more than one transfer
- [ ] Empty file
- [ ] Timeout

# Tests

<b> READ </b><br><br>

Normal <br>
- [ ] Single block
- [ ] Double block
- [ ] 512 bytes
- [ ] 132,000 bytes

Duplicate <br>
- [ ] RRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA (works for block 1 with 3 second delay)
- [ ] <b> Double block, ACK (Problem #5) </b>

Delay <br>
- [ ] RRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK

Lost <br>
- [ ] <b> RRQ </b>
- [ ] <b> Single block, DATA </b>
- [ ] <b> Single block, ACK </b>
- [ ] <b> Double block, DATA </b>
- [ ] <b> Double block, ACK </b>

<hr>

<b> WRITE </b><br><br>

Normal <br>
- [ ] Single block
- [ ] Double block
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
