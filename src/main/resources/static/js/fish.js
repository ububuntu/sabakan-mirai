const rodContainer = document.getElementById('fishing-rod');
const clickOverlay = document.getElementById('click-overlay');
const statusText = document.getElementById('status');
const waveContainer = document.getElementById('wave-container');

let isTsunamiActive = false;
let currentWeather = "Sunny";
let weatherInterval = null;
let escapeTimer = null;
let topRecords = [];

const tsunamiDiv = document.createElement('div');
tsunamiDiv.id = 'tsunami-overlay';
document.body.appendChild(tsunamiDiv);

// --- レアリティ設定 ---
const rarities = [
    { name: "Common", chance: 0.50, color: "#aaa", min: 1, max: 100, unit: "KB", list: ["空き缶", "流木", "スパゲッティコード", "空のCSV", "未解決のバグ"] },
    { name: "Uncommon", chance: 0.35, color: "#fff", min: 80, max: 500, unit: "MB", list: ["アジ >ﾟ)))彡", "イワシ >ﾟ))彡", "メダカ >ﾟ)彡", "イカ くコ:彡", "シーラカンス >ﾟ))))彡", "タツノオトシゴ <*))><", "深海魚 (°)))彡"] },
    { name: "Rare", chance: 0.14, color: "#0af", min: 450, max: 2000, unit: "GB", list: ["ERROR: 404 Not Found", "ノーチラス号", "地球", "AlloyDB", "月", "TODO: 後で直す", "完璧な正規表現", "README.md", "SLの破片"] },
    { name: "Legendary", chance: 0.01, color: "#f0f", min: 9999, max: 9999, unit: "PB", list: ["ビットコイン", "Hello,World!"] }
];

const weathers = {
    "Sunny": { color: "#004466", rate: 1.0 },
    "Rain":  { color: "#2d4a63", rate: 1.0 },
    "Storm": { color: "#1a1a2e", rate: 0.4 } // ヒット速度UP
};

// --- 天候演出ロジック ---
function updateWeatherVisuals() {
    if (weatherInterval) clearInterval(weatherInterval);
    if (currentWeather === "Rain" || currentWeather === "Storm") {
        weatherInterval = setInterval(() => {
            createRainDrop();
            if (currentWeather === "Storm" && Math.random() > 0.93) createLightning();
        }, 100);
    }
}

function createRainDrop() {
    const drop = document.createElement('div');
    drop.className = 'event-object';
    drop.innerText = "/";
    drop.style.left = Math.random() * 100 + "vw";
    drop.style.top = "-20px";
    drop.style.color = "rgba(174, 194, 224, 0.6)";
    drop.style.fontSize = "20px";
    drop.style.zIndex = "45";
    const fallTime = 500 + Math.random() * 500;
    drop.animate([
        { transform: 'translateY(0)' },
        { transform: `translateY(110vh) translateX(-50px)` }
    ], { duration: fallTime, easing: 'linear' });
    document.body.appendChild(drop);
    setTimeout(() => drop.remove(), fallTime);
}

function createLightning() {
    const lightning = document.createElement('div');
    lightning.className = 'event-object';
    const aaList = ["   ∠\n  /\n ∠\n/", "  |\n  L\n   |\n   L", "  _-\n   / \n  /  \n -_ "];
    lightning.innerText = aaList[Math.floor(Math.random() * aaList.length)];
    lightning.style.left = Math.random() * 90 + "vw";
    lightning.style.top = Math.random() * 30 + "vh";
    lightning.style.color = "#fff";
    lightning.style.fontSize = "40px";
    lightning.style.fontWeight = "bold";
    lightning.style.textShadow = "0 0 20px #fff";
    lightning.style.zIndex = "150";
    document.body.appendChild(lightning);

    const flash = document.createElement('div');
    flash.style.cssText = "position:fixed;top:0;left:0;width:100vw;height:100vh;background:rgba(255,255,255,0.2);z-index:140;pointer-events:none;";
    document.body.appendChild(flash);
    setTimeout(() => { lightning.remove(); flash.remove(); }, 150);
}

// --- 天候サイクル ---
function triggerWeatherCycle() {
    setTimeout(() => {
        if (!isTsunamiActive && currentWeather === "Sunny") startRain();
    }, Math.random() * 30000 + 30000);
}

function startRain() {
    currentWeather = "Rain";
    applyWeatherEffects();
    updateWeatherVisuals();
    statusText.innerHTML = `<div>しとしとと雨が降ってきた...</div>` + getRankingHTML(0.6);
    setTimeout(() => {
        statusText.innerHTML = `<div>天候が荒れそうだ...！</div>` + getRankingHTML(0.6);
        setTimeout(decideWeatherBranch, 3000);
    }, 15000);
}

function decideWeatherBranch() {
    const rand = Math.random() * 100;
    if (rand < 8) startTsunami();
    else if (rand < 40) startStorm();
    else backToSunny("雨が上がったようだ。");
}

function startStorm() {
    currentWeather = "Storm";
    applyWeatherEffects();
    updateWeatherVisuals();
    statusText.innerHTML = `<div style="color: #ffeb3b">⚡️ 雷雨：データノイズが激しい！（ヒット速度UP）</div>` + getRankingHTML(0.6);
    setTimeout(() => backToSunny("雷が遠ざかっていった..."), 25000);
}

function backToSunny(message) {
    currentWeather = "Sunny";
    applyWeatherEffects();
    if (weatherInterval) clearInterval(weatherInterval);
    statusText.innerHTML = `<div>${message}</div>` + getRankingHTML(0.6);
    triggerWeatherCycle();
}

function applyWeatherEffects() {
    document.body.style.backgroundColor = weathers[currentWeather].color;
}

// --- 釣りコアロジック ---
function startWaiting() {
    const weatherMod = weathers[currentWeather].rate;
    const baseWait = isTsunamiActive ? (Math.random() * 2000 + 1000) : (Math.random() * 27000 + 3000);
    const waitTime = baseWait * weatherMod;

    setTimeout(() => {
        rodContainer.classList.replace('sway-waiting', 'sway-hit');
        statusText.innerHTML = `<span style="color: #ff0">！！！ 画面をタップ ！！！</span>` + getRankingHTML(0.6);
        clickOverlay.classList.remove('hidden');
        const reactionLimit = Math.random() * 3600 + 400;
        escapeTimer = setTimeout(missed, reactionLimit);
        clickOverlay.onclick = () => { clearTimeout(escapeTimer); caught(); };
    }, waitTime);
}

function caught() {
    let rand = Math.random();
    if (isTsunamiActive) rand = (rand < 0.97) ? 0.86 : 0.995;

    let cumulativeChance = 0;
    let selectedRarity = rarities[0];
    let rarityIdx = 0;
    for (let i = 0; i < rarities.length; i++) {
        cumulativeChance += rarities[i].chance;
        if (rand < cumulativeChance) { selectedRarity = rarities[i]; rarityIdx = i; break; }
    }

    const result = selectedRarity.list[Math.floor(Math.random() * selectedRarity.list.length)];
    let finalSize = (selectedRarity.name === "Legendary") ? selectedRarity.max : Math.floor((selectedRarity.min + (selectedRarity.max - selectedRarity.min) * Math.random()) * ((Math.random() > 0.9) ? 1.2 : 1.0));

    updateTopRecords(result, finalSize, selectedRarity.unit, rarityIdx, selectedRarity.color);

    statusText.innerHTML = `
        <div style="font-size: 1.2em; color: ${selectedRarity.color}; margin-bottom: 10px;">
            【${isTsunamiActive ? '津波' : '釣果'}】 ${result} (${finalSize}${selectedRarity.unit})
        </div>
        ${getRankingHTML(0.8)}
    `;

    if (selectedRarity.name === "Legendary") {
        document.body.style.animation = "screen-shake 0.5s ease-in-out";
        setTimeout(() => document.body.style.animation = "", 500);
    }
    processNext();
}

function updateTopRecords(name, size, unit, rarityIdx, color) {
    topRecords.push({ name, size, unit, rarityIdx, color });
    topRecords.sort((a, b) => (b.rarityIdx !== a.rarityIdx) ? b.rarityIdx - a.rarityIdx : b.size - a.size);
    topRecords = topRecords.slice(0, 3);
}

function getRankingHTML(baseOpacity) {
    if (topRecords.length === 0) return "";
    const list = topRecords.map((r, i) => `<div style="color: ${r.color}; opacity: ${baseOpacity - i * 0.15}; font-size: 0.9em;">#${i+1}: ${r.name} (${r.size}${r.unit})</div>`).join("");
    return `<div style="margin-top: 10px; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 5px;">
                <div style="font-size: 0.7em; opacity: 0.5; margin-bottom: 3px;">--- DATA LAKE TOP 3 ---</div>${list}</div>`;
}

function missed() {
    statusText.innerHTML = `<span style="color: #f44">あぁっ！ 逃げられた...</span>` + getRankingHTML(0.5);
    processNext();
}

function processNext() {
    clickOverlay.classList.add('hidden');
    rodContainer.classList.replace('sway-hit', 'sway-waiting');
    setTimeout(resetGame, 2000);
}

function resetGame() {
    const title = isTsunamiActive ? "⚠️ 津波警報：爆釣モード！" : (currentWeather === "Storm" ? "⚡️ 雷雨：激しいノイズ！" : "ようこそ、くつろいでいってよ。");
    statusText.style.color = isTsunamiActive ? "#00ffff" : "#fff";
    statusText.innerHTML = `<div>${title}</div>` + getRankingHTML(0.7);
    startWaiting();
}

// --- イベント演出 ---
function startTsunami() {
    isTsunamiActive = true;
    if (weatherInterval) clearInterval(weatherInterval);
    tsunamiDiv.classList.add('active');
    statusText.innerText = "⚠️ 津波警報：爆釣モード！";
    statusText.style.color = "#00ffff";
    const floaters = [];
    for (let i = 0; i < 20; i++) {
        const f = document.createElement('div');
        f.className = 'floater event-object';
        f.innerText = [">ﾟ)))彡", "くコ:彡", ">ﾟ)彡", "(°)))彡", "<*))><"][Math.floor(Math.random() * 3)];
        f.style.left = Math.random() * 90 + "%";
        f.style.top = Math.random() * 80 + "%";
        document.body.appendChild(f);
        floaters.push(f);
    }
    setTimeout(() => {
        isTsunamiActive = false;
        tsunamiDiv.classList.remove('active');
        floaters.forEach(f => f.remove());
        backToSunny("波が引いていった...");
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
            const creature = document.createElement('div');
            creature.className = 'event-object';
            creature.style.bottom = (50 + Math.random() * 100) + "px";
            creature.style.left = "0";
            creature.style.zIndex = "30";
            if (Math.random() < 0.2) {
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

function triggerRandomEvent() {
    if (isTsunamiActive || currentWeather !== "Sunny") {
        setTimeout(triggerRandomEvent, 10000);
        return;
    }
    const eventRand = Math.random();
    if (eventRand < 0.15) spawnSL();
    else if (eventRand < 0.45) spawnJumpingSchool();
    setTimeout(triggerRandomEvent, Math.random() * 15000 + 15000);
}

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

// 初期化
createWave();
startWaiting();
triggerWeatherCycle();
triggerRandomEvent();