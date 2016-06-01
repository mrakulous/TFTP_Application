# Everything we need

* Verbose/Quiet
* Error/NoError
* Server shutdown
* Continue transfer
* Empty file

<hr>

<b> READ </b><br><br>

Normal <br>
* Single block, DATA
* Single block, ACK
* Double block, DATA
* Double block, ACK
* 512 bytes (1 block) <br>
* 132,000 bytes (255+ blocks) <br>

Duplicate <br>
* Single block, DATA
* Single block, ACK
* Double block, DATA
* Double block, ACK
* 512 bytes (1 block) <br>
* 132,000 bytes (255+ blocks) <br>

Delay <br>
* Single block, DATA
* Single block, ACK
* Double block, DATA
* Double block, ACK
* 512 bytes (1 block) <br>
* 132,000 bytes (255+ blocks) <br>

Lost <br><br>
* Single block, DATA
* Single block, ACK
* Double block, DATA
* Double block, ACK
* 512 bytes (1 block) <br>
* 132,000 bytes (255+ blocks) <br>

<hr>

<b> WRITE </b><br>

Normal <br>
* Single block, DATA
* Single block, ACK
* Double block, DATA
* Double block, ACK
* 512 bytes (1 block) <br>
* 132,000 bytes (255+ blocks) <br>

Duplicate <br>
* Single block, DATA
* Single block, ACK
* Double block, DATA
* Double block, ACK
* 512 bytes (1 block) <br>
* 132,000 bytes (255+ blocks) <br>

Delay <br>
* Single block, DATA
* Single block, ACK
* Double block, DATA
* Double block, ACK
* 512 bytes (1 block) <br>
* 132,000 bytes (255+ blocks) <br>

Lost <br><br>
* Single block, DATA
* Single block, ACK
* Double block, DATA
* Double block, ACK
* 512 bytes (1 block) <br>
* 132,000 bytes (255+ blocks) <br>

<hr>

Work in-progress for Iteration 2

1. Fix file creation
2. stops after one
3. implement 512, 0bytes case
4. breaks at 8k
5. shutdown server
6. timeout client if no server
7. try and few times and then timeout
8. client shutdowns with user input q
