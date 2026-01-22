// ポップアップメッセージを表示する関数
function showPopupMessage(message) {
    // ポップアップ要素を作成
    const popup = document.createElement('div');
    popup.className = 'popup-message';

    // メッセージテキスト
    const text = document.createElement('span');
    text.className = 'popup-message-text';
    text.textContent = message;

    // プログレスバーコンテナ
    const progressContainer = document.createElement('div');
    progressContainer.className = 'popup-message-progress';

    // プログレスバー
    const progressBar = document.createElement('div');
    progressBar.className = 'popup-message-progress-bar';
    progressContainer.appendChild(progressBar);

    popup.appendChild(text);
    popup.appendChild(progressContainer);

    // bodyに追加
    document.body.appendChild(popup);

    // スライドダウンアニメーション開始
    setTimeout(() => {
        popup.classList.add('show');
    }, 10);

    // プログレスバーのアニメーション
    const displayDuration = 3000; // 表示時間3秒
    const slideUpDuration = 400; // スライドアップ0.4秒
    const totalDuration = displayDuration + slideUpDuration;
    const interval = 100; // 100ms毎に更新
    let elapsed = 0;

    const timer = setInterval(() => {
        elapsed += interval;
        const progress = Math.max(0, 100 - (elapsed / displayDuration * 100));
        progressBar.style.width = progress + '%';

        if (elapsed >= displayDuration) {
            clearInterval(timer);
            // プログレスバーが0になったらスライドアップ開始
            popup.classList.remove('show');
            popup.classList.add('hide');

            // スライドアップアニメーション完了後に削除
            setTimeout(() => {
                popup.remove();
            }, slideUpDuration);
        }
    }, interval);
}

// ページ読み込み時にメッセージがあれば表示
document.addEventListener('DOMContentLoaded', function() {
    const messageElement = document.querySelector('[data-message]');
    if (messageElement) {
        const message = messageElement.textContent.trim();
        if (message) {
            showPopupMessage(message);
        }
    }
});