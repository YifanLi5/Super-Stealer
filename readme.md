# Super Stealer : Mark & Thieve

Osbot thieving
script for pickpocketing any valid NPC in the game!

## Features:
- Restocks from nearest bank when out of supplies
    * remembers starting inventory, restocks will restore inventory to starting state
- Will be active even while stunned. Will randomly pick an action from the following.
<br> 
  * Eat (Will eat at Random HP)
  * Spam pickpocket
  * Drop Junk (ex: If using jugs of wine)
  * AFK
- Dodgy Necklace support
- Shadow veil (In Progress - requires account progression)
- CLI support (In Progress)

## Other Notes:
- Script will not start with full inventory. Accommodate for this by leaving enough open slots for coin pouch, coins, other loot
  * banking task is triggered if the inventory is full. (For master farmers)
- If start this script in Ardougne South Bank AND are pickpocketing Ardougne Knights. 
<br>This script will stop if your character leaves the Ardougne South Bank. 
  * Assumed to be in Ardougne knight mass pickpocket with a splasher.

  
![mark_n_thieve1.jpg](readme_imgs%2Fmark_n_thieve1.jpg)

## Proggies
![1hr.JPG](readme_imgs%2F1hr.JPG)
![pally_1hr.JPG](readme_imgs%2Fpally_1hr.JPG)
![pally_1hr2.JPG](readme_imgs%2Fpally_1hr2.JPG)

## Todo List:
- check if splash host is still splashing (If doing mass ardy knight)
- Add emergency run away, if player is under attack
- shadow veil (Once I get access to spell)
- cli support, (save start location + inventory setup + npc selection)