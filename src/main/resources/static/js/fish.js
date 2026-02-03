const rodContainer = document.getElementById('fishing-rod');
const clickOverlay = document.getElementById('click-overlay');
const statusText = document.getElementById('status');
const waveContainer = document.getElementById('wave-container');

// レアリティ設定
const rarities = [
    { name: "Common", chance: 0.50, color: "#aaa", list: ["長靴", "空き缶", "流木", "スパゲッティコード"] },
    { name: "Uncommon", chance: 0.35, color: "#fff", list: ["アジ >ﾟ)))彡", "イワシ >ﾟ))彡", "メダカ >ﾟ)彡", "イカ くコ:彡", "シーラカンス >ﾟ))))彡"] },
    { name: "Rare", chance: 0.14, color: "#0af", list: ["ERROR: 404 Not Found", "ノーチラス号", "地球", "AlloyDB"] },
    { name: "Legendary", chance: 0.01, color: "#f0f", list: ["ビットコイン"] }
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
    const waitTime = Math.random() * 27000 + 3000;
    setTimeout(() => {
        rodContainer.classList.replace('sway-waiting', 'sway-hit');
        statusText.innerText = "！！！ 画面をタップ ！！！";
        statusText.style.color = "#ff0";
        clickOverlay.classList.remove('hidden');

        // 逃走までの時間もランダム (0.4s 〜 4.0s)
        const reactionLimit = Math.random() * 3600 + 400;
        escapeTimer = setTimeout(missed, reactionLimit);

        clickOverlay.onclick = () => {
            clearTimeout(escapeTimer);
            caught();
        };
    }, waitTime);
}

function caught() {
    // 確率計算
    const rand = Math.random();
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

    // 演出
    statusText.innerText = `【釣果】 ${result}`;
    statusText.style.color = selectedRarity.color;

    // レジェンダリー（root）の場合は画面を揺らす演出
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
    statusText.innerText = "ようこそ、くつろいでいってよ。";
    statusText.style.color = "#fff";
    startWaiting();
}

createWave();
startWaiting();