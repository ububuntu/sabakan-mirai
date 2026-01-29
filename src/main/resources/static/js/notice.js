/**
 * notice.js
 * 上からスライドインし、3秒後に消える通知機能
 */
document.addEventListener('DOMContentLoaded', () => {
    // 通知用要素を作成
    const notice = document.createElement('div');
    notice.className = 'modal-notice';
    document.body.appendChild(notice);


    const showModalNotice = (message, isError = false) => {
        if (!message) return;

        notice.textContent = message;

        if (isError) {
            notice.classList.add('is-error');
        } else {
            notice.classList.remove('is-error');
        }

        // 表示開始
        notice.classList.add('is-visible');

        // 3秒後に非表示にする
        setTimeout(() => {
            notice.classList.remove('is-visible');
        }, 3000);
    };

    // HTML内に埋め込まれたエラーメッセージを取得して実行
    const errorElement = document.querySelector('.error-message-data');
    if (errorElement && errorElement.textContent.trim() !== "") {
        showModalNotice(errorElement.textContent, true);
    }
});