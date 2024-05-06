# Super Stealer : Mark & Thieve

Osbot thieving script for pickpocketing any valid NPC in the game!

## Setup:
- Setup starting inventory manually ingame
- Start Script, Click NPCs highlighted by painter. <span style="color: green;">(Green = selected)</span>
- Click Finish Selection

![visual_instructions.jpg](readme_imgs%2Fvisual_instructions.jpg)

**You must leave empty slots for pickpocket loot. Minimum 2 (coins and coin pouch). 
<br>
Adjust accordingly for different NPCs.**

![inv_setup.jpg](readme_imgs%2Finv_setup.jpg)

## Features:
- Restocks from nearest bank when out of supplies
    * remembers starting inventory, restocks will restore inventory to starting state
- Will be active even while stunned. Will randomly pick an action from the following.
  * Eat (Will eat at Random HP)
  * Spam pickpocket
  * Drop Junk (ex: If using jugs of wine)
  * AFK
- Dodgy Necklace support
  * Start with necklaces in inventory
- Shadow veil
  * Start staff equipped + runes in inventory
- Adjusts Maximum coin pouch stack based on character's Ardougne diary progression. 
  * (None/Easy/: 28, Medium: 56, Hard: 84, Elite: 140)
- Should support all Food and jugs of wine. 

- CLI support (In Progress)


## Other Notes:
- Emergency eat is @ <= 5Hp. Generally mid stun eating will offset damage taken. 
- Script will not start with full inventory. Accommodate for this by leaving enough open slots for coin pouch, coins, other loot
  * banking task is triggered if the inventory is full. (For master farmers)
- If start this script in Ardougne South Bank AND are pickpocketing Ardougne Knights. 
<br>This script will stop if your character leaves the Ardougne South Bank. 
  * Assumed to be in Ardougne knight mass pickpocket with a splash host.


## Proggies
![1hr.JPG](readme_imgs%2F1hr.JPG)
![pally_1hr.JPG](readme_imgs%2Fpally_1hr.JPG)
![pally_1hr2.JPG](readme_imgs%2Fpally_1hr2.JPG)

## Todo List:
- check if splash host is still splashing (If doing mass ardy knight)
- Add emergency run away, if player is under attack
- cli support, (save start location + inventory setup + npc selection)