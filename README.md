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
- [ ] Double block, DATA
- [ ] Double block, ACK

Delay <br>
- [ ] RRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK

Lost <br>
- [ ] RRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK

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
