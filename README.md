# Problems
- [x] <b> 1. </b> Server prints wrong byte array length (Alagu)
- [ ] <b> 2. </b> Read, duplicate, RRQ print statement
- [ ] <b> 3. </b> Read, duplicate, DATA, needs to be handled properly by the client
- [ ] <b> 4. </b> Read, normal, only reads ~65000 bytes for a 132000 bytes file
- [ ] <b> 5. </b> Read, duplicate, ACK print statement
- [ ] <b> 6. </b> Should not be able to pick for example, WRQ when doing a read nor RRQ when writing
- [ ] <b> 7. </b> Invalid block number input is not handled

# Things We Need

- [x] <b> Verbose/Quiet </b>
- [x] <b> Error/NoError </b>
- [ ] <b> Server shutdown </b>
- [x] <b> Allow more than one transfer </b>
- [x] Empty file [June 2]
- [x] <b> Timeout </b>

# Tests

<b> READ </b><br><br>

Normal <br>
- [x] Single block [June 2]
- [x] Double block [June 2]
- [x] 512 bytes [June 2]
- [ ] <b> 132,000 bytes (Problem #4) </b>

Duplicate <br>
- [ ] <b> RRQ (Problem #2) </b>
- [ ] <b> Single block, DATA (Problem #3) </b>
- [ ] <b> Single block, ACK (Problem #5) </b>
- [ ] <b> Double block, DATA (Problem #3) </b>
- [ ] <b> Double block, ACK (Problem #5) </b>

Delay <br>
- [x] RRQ [June 2]
- [x] Single block, DATA [June 2]
- [x] Single block, ACK [June 2]
- [x] Double block, DATA [June 2]
- [x] Double block, ACK [June 2]

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

# Problems from TA test

1. Fix file creation
2. stops after one
3. implement 512, 0bytes case
4. breaks at 8k
5. shutdown server
6. timeout client if no server
7. try and few times and then timeout
8. client shutdowns with user input q
