const rodContainer = document.getElementById('fishing-rod');
const clickOverlay = document.getElementById('click-overlay');
const statusText = document.getElementById('status');
const waveContainer = document.getElementById('wave-container');

const fishList = ["アジ >ﾟ)))彡", "マグロ >°))))彡", "長靴 (ﾟ∀ﾟ)!!", "粗大ゴミ [ゴミ箱]"];
let escapeTimer = null;

// ランダムな波を生成
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
    const waitTime = Math.random() * 5000 + 2000;
    setTimeout(() => {
        // HIT発生
        rodContainer.classList.replace('sway-waiting', 'sway-hit');
        statusText.innerText = "！！！ 画面をタップ ！！！";
        statusText.style.color = "#ff0";

        // 画面全体のクリックを有効化
        clickOverlay.classList.remove('hidden');

        escapeTimer = setTimeout(missed, 2500);

        clickOverlay.onclick = () => {
            clearTimeout(escapeTimer);
            caught();
        };
    }, waitTime);
}

function caught() {
    const result = fishList[Math.floor(Math.random() * fishList.length)];
    statusText.innerText = `【釣果】 ${result}`;
    statusText.style.color = "#0f0";
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