const rodContainer = document.getElementById('fishing-rod');
const clickOverlay = document.getElementById('click-overlay');
const statusText = document.getElementById('status');
const waveContainer = document.getElementById('wave-container');

// 津波イベント用のフラグと要素
let isTsunamiActive = false;
const tsunamiDiv = document.createElement('div');
tsunamiDiv.id = 'tsunami-overlay';
document.body.appendChild(tsunamiDiv);

// レアリティ設定
const rarities = [
    { name: "Common", chance: 0.50, color: "#aaa", list: ["長靴", "空き缶", "流木", "スパゲッティコード"] },
    { name: "Uncommon", chance: 0.35, color: "#fff", list: ["アジ >ﾟ)))彡", "イワシ >ﾟ))彡", "メダカ >ﾟ)彡", "イカ くコ:彡", "シーラカンス >ﾟ))))彡"] },
    { name: "Rare", chance: 0.14, color: "#0af", list: ["ERROR: 404 Not Found", "ノーチラス号", "地球", "AlloyDB"] },
    { name: "Legendary", chance: 0.01, color: "#f0f", list: ["ビットコイン", "root 権限 (☆∀☆)"] }
];

let escapeTimer = null;

function createWave() {
    const wave = document.createElement('div');
    wave.className = 'random-wave';
    wave.innerText = '〜〜〜';
    wave.style.left = Math.random() * 80 + 10 + "%";
    wave.style.top = Math.random() * 60 + 20 + "%";
    waveContainer.appendChild(wave);
    setTimeout(() => wave.remove(), 3000);
    setTimeout(createWave, Math.random() * 2000 + 1000);
}

function startWaiting() {
    // 津波中は爆速（1〜3秒）、通常は（3〜30秒）
    const waitTime = isTsunamiActive
        ? Math.random() * 2000 + 1000
        : Math.random() * 27000 + 3000;

    setTimeout(() => {
        rodContainer.classList.replace('sway-waiting', 'sway-hit');
        statusText.innerText = "！！！ 画面をタップ ！！！";
        statusText.style.color = "#ff0";
        clickOverlay.classList.remove('hidden');

        const reactionLimit = Math.random() * 3600 + 400;
        escapeTimer = setTimeout(missed, reactionLimit);

        clickOverlay.onclick = () => {
            clearTimeout(escapeTimer);
            caught();
        };
    }, waitTime);
}

function caught() {
    let rand = Math.random();

    if (isTsunamiActive) {
        if (rand < 0.97) {
            rand = 0.86; // Rareの範囲内（Common/Uncommonをスキップ）
        } else {
            rand = 0.995; // Legendaryの範囲内
        }
    }

    let cumulativeChance = 0;
    let selectedRarity = rarities[0];
    for (const r of rarities) {
        cumulativeChance += r.chance;
        if (rand < cumulativeChance) {
            selectedRarity = r;
            break;
        }
    }

    const result = selectedRarity.list[Math.floor(Math.random() * selectedRarity.list.length)];
    statusText.innerText = `【${isTsunamiActive ? '津波の恵み' : '釣果'}】 ${result}`;
    statusText.style.color = selectedRarity.color;

    if (selectedRarity.name === "Legendary") {
        document.body.style.animation = "screen-shake 0.5s ease-in-out";
        setTimeout(() => document.body.style.animation = "", 500);
    }
    processNext();
}

function missed() {
    statusText.innerText = "あぁっ！ 逃げられた...";
    statusText.style.color = "#f44";
    processNext();
}

function processNext() {
    clickOverlay.classList.add('hidden');
    rodContainer.classList.replace('sway-hit', 'sway-waiting');
    setTimeout(resetGame, 2000);
}

function resetGame() {
    statusText.innerText = isTsunamiActive ? "⚠️ 津波警報：爆釣モード！" : "ようこそ、くつろいでいってよ。";
    statusText.style.color = isTsunamiActive ? "#00ffff" : "#fff";
    startWaiting();
}

function triggerRandomEvent() {
    if (isTsunamiActive) {
        setTimeout(triggerRandomEvent, 10000);
        return;
    }
    const eventRand = Math.random();
    if (eventRand < 0.10) startTsunami();
    else if (eventRand < 0.25) spawnSL();
    else if (eventRand < 0.55) spawnJumpingSchool();

    setTimeout(triggerRandomEvent, Math.random() * 15000 + 15000);
}

function startTsunami() {
    isTsunamiActive = true;
    tsunamiDiv.classList.add('active');
    statusText.innerText = "⚠️ 津波警報：爆釣モード！";
    statusText.style.color = "#00ffff";

    const floaters = [];
    const icons = [">ﾟ)))彡", "くコ:彡", "AlloyDB"];
    for (let i = 0; i < 20; i++) {
        const f = document.createElement('div');
        f.className = 'floater event-object';
        f.innerText = icons[Math.floor(Math.random() * icons.length)];
        f.style.left = Math.random() * 90 + "%";
        f.style.top = Math.random() * 80 + "%";
        f.style.color = "rgba(136, 170, 255, 0.6)";
        f.style.zIndex = "40";
        document.body.appendChild(f);
        floaters.push(f);
    }

    setTimeout(() => {
        isTsunamiActive = false;
        tsunamiDiv.classList.remove('active');
        floaters.forEach(f => f.remove());
        if (!rodContainer.classList.contains('sway-hit')) {
            statusText.innerText = "波が引いていった...";
            statusText.style.color = "#fff";
        }
    }, 100000);
}

function spawnSL() {
    const sl = document.createElement('div');
    sl.className = 'event-object';
    sl.style.right = "-200px";
    sl.style.bottom = "-100px";
    sl.style.color = "#ddd";
    sl.style.fontSize = "10px";
    sl.style.textShadow = "0 0 15px rgba(255, 255, 255, 0.7)";
    sl.style.zIndex = "30";
    sl.style.animation = "sl-galaxy-move 12s linear forwards";
    sl.innerText = `
          ====        ________                ___________
      _D _|  |_______/        \\__I_I_____===__|_________|
       |(_)---  |   H\\________/ |   |        =|___ ___|      _________________
       /     |  |   H  |  |     |   |         ||_| |_||     _|                \\_____A
      |      |  |   H  |__--------------------| [___] |   =|                        |
      | ________|___H__/__|_____/[][]~\\_______|       |   -|                        |
      |/ |   |-----------I_____I [][] []  D   |=======|____|________________________|_
    __/ =| o |=-~~\\  /~~\\  /~~\\  /~~\\ ____Y___________|__|__________________________|_
     |/-=|___||    ||    ||    ||    |_____/~\\___/          |_D__D__D_|  |_D__D__D_|
      \\_/      \\__/  \\__/  \\__/  \\__/      \\_/               \\_/   \\_/    \\_/   \\_/
    `;
    document.body.appendChild(sl);
    setTimeout(() => sl.remove(), 12500);
}

function spawnJumpingSchool() {
    const count = 3 + Math.floor(Math.random() * 5);
    for (let i = 0; i < count; i++) {
        setTimeout(() => {
            const isSquid = Math.random() < 0.2;
            const creature = document.createElement('div');
            creature.className = 'event-object';
            creature.style.bottom = (50 + Math.random() * 100) + "px";
            creature.style.left = "0";
            creature.style.zIndex = "30";
            if (isSquid) {
                creature.innerText = "くコ:彡";
                creature.style.color = "#ffb6c1";
                creature.style.animation = "squid-jump 3.5s ease-in-out forwards";
            } else {
                creature.innerText = ">ﾟ)))彡";
                creature.style.color = "#8af";
                creature.style.animation = "jump-move 3s ease-in-out forwards";
            }
            document.body.appendChild(creature);
            setTimeout(() => creature.remove(), 4000);
        }, i * 400);
    }
}

createWave();
startWaiting();
triggerRandomEvent();