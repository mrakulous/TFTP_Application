# Things We Need

- [ ] Verbose/Quiet
- [ ] Error/NoError
- [ ] Server shutdown
- [ ] Allow more than one transfer
- [ ] Empty file (makes a file with 4 blank bytes)
- [ ] Timeout

<hr>

<b> READ </b><br><br>

Normal <br>
- [x] Single block [June 1]
- [x] Double block [June 1]
- [x] 512 bytes [June 1]
- [ ] 132,000 bytes (Only reads ~65000 bytes)

Duplicate <br>
- [ ] RRQ
- [ ] WRQ
- [ ] Single block, DATA 
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK
- [ ] 512 bytes
- [ ] 132,000 bytes

Delay <br>
- [x] RRQ [June 1]
- [x] WRQ [June 1]
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK
- [ ] 512 bytes
- [ ] 132,000 bytes

Lost <br>
- [ ] RRQ
- [ ] WRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK
- [ ] 512 bytes
- [ ] 132,000 bytes

<hr>

<b> WRITE </b><br><br>

Normal <br>
- [ ] Single block
- [ ] Double block
- [ ] 512 bytes
- [ ] 132,000 bytes

Duplicate <br>
- [ ] RRQ
- [ ] WRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK
- [ ] 512 bytes
- [ ] 132,000 bytes

Delay <br>
- [ ] RRQ
- [ ] WRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK
- [ ] 512 bytes
- [ ] 132,000 bytes

Lost <br>
- [ ] RRQ
- [ ] WRQ
- [ ] Single block, DATA
- [ ] Single block, ACK
- [ ] Double block, DATA
- [ ] Double block, ACK
- [ ] 512 bytes
- [ ] 132,000 bytes

# Problems from TA test

1. Fix file creation
2. stops after one
3. implement 512, 0bytes case
4. breaks at 8k
5. shutdown server
6. timeout client if no server
7. try and few times and then timeout
8. client shutdowns with user input q
