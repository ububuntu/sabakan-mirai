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

function triggerRandomEvent() {
    const eventRand = Math.random();

    if (eventRand < 0.1) {
        spawnSL(); // 10%で汽車
    } else if (eventRand < 0.4) {
        spawnJumpingSchool(); // 30%で群れが出現
    }

    // 次のイベント抽選 (10秒〜20秒おき)
    setTimeout(triggerRandomEvent, Math.random() * 10000 + 10000);
}

// 汽車 (SL)
function spawnSL() {
    const sl = document.createElement('div');
    sl.className = 'event-object';
    sl.style.bottom = "10px";
    sl.style.color = "#888";
    sl.style.fontSize = "12px";
    sl.style.animation = "sl-move 10s linear forwards";
    sl.innerText = `
          ====        ________                ___________
      _D _|  |_______/        \__I_I_____===__|_________|
       |(_)---  |   H\________/ |   |        =|___ ___|      _________________
       /     |  |   H  |  |     |   |         ||_| |_||     _|                \_____A
      |      |  |   H  |__--------------------| [___] |   =|                        |
      | ________|___H__/__|_____/[][]~\_______|       |   -|                        |
      |/ |   |-----------I_____I [][] []  D   |=======|____|________________________|_
    __/ =| o |=-~~\  /~~\  /~~\  /~~\ ____Y___________|__|__________________________|_
     |/-=|___||    ||    ||    ||    |_____/~\___/          |_D__D__D_|  |_D__D__D_|
      \_/      \__/  \__/  \__/  \__/      \_/               \_/   \_/    \_/   \_/
    `;
    document.body.appendChild(sl);
    setTimeout(() => sl.remove(), 10500);
}

// 魚とイカの群れ
function spawnJumpingSchool() {
    const count = 3 + Math.floor(Math.random() * 5); // 3〜7匹

    for (let i = 0; i < count; i++) {
        setTimeout(() => {
            const isSquid = Math.random() < 0.2; // 20%の確率でイカ
            const creature = document.createElement('div');
            creature.className = 'event-object';

            // 出現位置の高さをバラけさせる
            creature.style.bottom = (50 + Math.random() * 100) + "px";
            creature.style.left = "0";

            if (isSquid) {
                creature.innerText = "くコ:彡";
                creature.style.color = "#ffb6c1"; // イカは薄ピンク
                creature.style.animation = "squid-jump 3.5s ease-in-out forwards";
            } else {
                creature.innerText = ">ﾟ)))彡";
                creature.style.color = "#8af"; // 魚は水色
                creature.style.animation = "jump-move 3s ease-in-out forwards";
            }

            document.body.appendChild(creature);
            setTimeout(() => creature.remove(), 4000);
        }, i * 400); // 0.4秒間隔で次々とはねる
    }
}

createWave();
startWaiting();
triggerRandomEvent();