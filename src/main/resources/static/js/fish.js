const rodContainer = document.getElementById('fishing-rod');
const clickOverlay = document.getElementById('click-overlay');
const statusText = document.getElementById('status');
const waveContainer = document.getElementById('wave-container');

let isTsunamiActive = false;
const tsunamiDiv = document.createElement('div');
tsunamiDiv.id = 'tsunami-overlay';
document.body.appendChild(tsunamiDiv);

// レアリティ設定
const rarities = [
    { name: "Common", chance: 0.50, color: "#aaa", min: 1, max: 100, unit: "KB", list: ["空き缶", "流木", "スパゲッティコード", "空のCSV", "未解決のバグ"] },
    { name: "Uncommon", chance: 0.35, color: "#fff", min: 80, max: 500, unit: "MB", list: ["アジ >ﾟ)))彡", "イワシ >ﾟ))彡", "メダカ >ﾟ)彡", "イカ くコ:彡", "シーラカンス >ﾟ))))彡", "タツノオトシゴ <*))><", "深海魚 (°)))彡"] },
    { name: "Rare", chance: 0.14, color: "#0af", min: 450, max: 2000, unit: "GB", list: ["ERROR: 404 Not Found", "ノーチラス号", "地球", "AlloyDB", "月", "TODO: 後で直す", "完璧な正規表現", "README.md", "SLの破片"] },
    { name: "Legendary", chance: 0.01, color: "#f0f", min: 9999, max: 9999, unit: "PB", list: ["ビットコイン", "Hello,World!"] }
];

let escapeTimer = null;
// 上位3つの記録を保持する配列
let topRecords = [];

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
    const waitTime = isTsunamiActive ? Math.random() * 2000 + 1000 : Math.random() * 27000 + 3000;
    setTimeout(() => {
        rodContainer.classList.replace('sway-waiting', 'sway-hit');
        statusText.innerText = "！！！ 画面をタップ ！！！";
        statusText.style.color = "#ff0";
        clickOverlay.classList.remove('hidden');
        const reactionLimit = Math.random() * 3600 + 400;
        escapeTimer = setTimeout(missed, reactionLimit);
        clickOverlay.onclick = () => { clearTimeout(escapeTimer); caught(); };
    }, waitTime);
}

function caught() {
    let rand = Math.random();
    if (isTsunamiActive) {
        rand = (rand < 0.97) ? 0.86 : 0.995;
    }

    let cumulativeChance = 0;
    let selectedRarity = rarities[0];
    let rarityIdx = 0;
    for (let i = 0; i < rarities.length; i++) {
        cumulativeChance += rarities[i].chance;
        if (rand < cumulativeChance) {
            selectedRarity = rarities[i];
            rarityIdx = i;
            break;
        }
    }

    const result = selectedRarity.list[Math.floor(Math.random() * selectedRarity.list.length)];

    // サイズ計算
    let finalSize;
    if (selectedRarity.name === "Legendary") {
        finalSize = selectedRarity.max;
    } else {
        const base = Math.random();
        const boost = (Math.random() > 0.9) ? 1.2 : 1.0;
        finalSize = Math.floor((selectedRarity.min + (selectedRarity.max - selectedRarity.min) * base) * boost);
    }

    // 記録を更新・ソート
    updateTopRecords(result, finalSize, selectedRarity.unit, rarityIdx, selectedRarity.color);

    // 表示
    const topDisplay = topRecords.map((r, i) =>
        `<div style="color: ${r.color}; opacity: ${1 - i * 0.2};">#${i+1}: ${r.name} (${r.size}${r.unit})</div>`
    ).join("");

    statusText.innerHTML = `
        <div style="font-size: 1.2em; color: ${selectedRarity.color}; margin-bottom: 10px;">
            【${isTsunamiActive ? '津波の恵み' : '釣果'}】 ${result} (${finalSize}${selectedRarity.unit})
        </div>
        <div style="font-size: 0.8em; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 5px;">
            <div style="margin-bottom: 3px; opacity: 0.6;">--- DATA LAKE TOP 3 ---</div>
            ${topDisplay}
        </div>
    `;

    if (selectedRarity.name === "Legendary") {
        document.body.style.animation = "screen-shake 0.5s ease-in-out";
        setTimeout(() => document.body.style.animation = "", 500);
    }
    processNext();
}

function updateTopRecords(name, size, unit, rarityIdx, color) {
    topRecords.push({ name, size, unit, rarityIdx, color });

    // レアリティ指数(PB > GB...)とサイズでソート
    topRecords.sort((a, b) => {
        if (b.rarityIdx !== a.rarityIdx) return b.rarityIdx - a.rarityIdx;
        return b.size - a.size;
    });

    // 上位3つだけ残す
    topRecords = topRecords.slice(0, 3);
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
    statusText.style.color = isTsunamiActive ? "#00ffff" : "#fff";

    // 待機中もランキング（名前 + サイズ + 単位）を表示し続けるように修正
    const topDisplay = topRecords.length > 0
        ? `<div style="font-size: 0.7em; margin-top: 10px; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 5px;">
           <div style="margin-bottom: 3px; opacity: 0.6;">--- DATA LAKE TOP 3 ---</div>
           ${topRecords.map((r, i) =>
               `<div style="color: ${r.color}; opacity: ${0.8 - i * 0.2};">
                   #${i+1}: ${r.name} (${r.size}${r.unit})
                </div>`
           ).join("")}</div>`
        : "";

    statusText.innerHTML = (isTsunamiActive ? "⚠️ 津波警報：爆釣モード！" : "ようこそ、くつろいでいってよ。") + topDisplay;
    startWaiting();
}

function triggerRandomEvent() {
    if (isTsunamiActive) { setTimeout(triggerRandomEvent, 10000); return; }
    const eventRand = Math.random();
    if (eventRand < 0.05) startTsunami();
    else if (eventRand < 0.15) spawnSL();
    else if (eventRand < 0.45) spawnJumpingSchool();
    setTimeout(triggerRandomEvent, Math.random() * 15000 + 15000);
}

function startTsunami() {
    isTsunamiActive = true;
    tsunamiDiv.classList.add('active');
    statusText.innerText = "⚠️ 津波警報：爆釣モード！";
    statusText.style.color = "#00ffff";
    const floaters = [];
    for (let i = 0; i < 20; i++) {
        const f = document.createElement('div');
        f.className = 'floater event-object';
        f.innerText = [">ﾟ)))彡", "くコ:彡", "AlloyDB"][Math.floor(Math.random() * 6)];
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
    }, 20000);
}

function spawnSL() {
    const sl = document.createElement('div');
    sl.className = 'event-object';
    sl.style.right = "-200px"; sl.style.bottom = "-100px";
    sl.style.color = "#ddd"; sl.style.fontSize = "10px";
    sl.style.textShadow = "0 0 15px rgba(255, 255, 255, 0.7)";
    sl.style.zIndex = "30"; sl.style.animation = "sl-galaxy-move 12s linear forwards";
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
                creature.innerText = "くコ:彡"; creature.style.color = "#ffb6c1";
                creature.style.animation = "squid-jump 3.5s ease-in-out forwards";
            } else {
                creature.innerText = ">ﾟ)))彡"; creature.style.color = "#8af";
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