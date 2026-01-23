/**
 * Cookie操作用のユーティリティ関数
 */
const CookieManager = {
    // Cookieを設定
    set(name, value, days = 365) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        const expires = `expires=${date.toUTCString()}`;
        // セキュリティ向上のためSameSite属性を付与
        document.cookie = `${name}=${value};${expires};path=/;SameSite=Lax`;
    },

    // Cookieを取得
    get(name) {
        const nameEQ = `${name}=`;
        const ca = document.cookie.split(';');
        for (let i = 0; i < ca.length; i++) {
            let c = ca[i].trim();
            if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    // Cookieを削除
    delete(name) {
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
    }
};

/**
 * テーマ変更関数
 * @param {string} theme - 'default', 'dark', 'qwer' など
 */
function changeTheme(theme) {
    if (!theme) return;
    // data-theme属性を設定
    document.documentElement.setAttribute('data-theme', theme);

    // Cookieに保存（1年間有効）
    CookieManager.set('theme', theme, 365);

    console.log('テーマを変更しました:', theme);
}

/**
 * テーマをリセットする関数
 */
function resetTheme() {
    CookieManager.delete('theme');
    document.documentElement.setAttribute('data-theme', 'default');
    const colorSelect = document.getElementById('color');
    if (colorSelect) {
        colorSelect.value = 'default';
    }
    console.log('テーマをリセットしました');
}

/**
 * ページ読み込み時にテーマを復元し、システムメッセージを表示する
 */
window.addEventListener('DOMContentLoaded', () => {
    // --- 1. テーマの復元処理 ---
    const savedTheme = CookieManager.get('theme') || 'default';
    document.documentElement.setAttribute('data-theme', savedTheme);

    const colorSelect = document.getElementById('color');
    if (colorSelect) {
        colorSelect.value = savedTheme;
    }
    console.log('保存されたテーマを読み込みました:', savedTheme);

    // --- 2. メッセージ表示機能（既存のHTML要素を利用） ---
    // HTML内の [data-message] 属性を持つ要素を取得
    const messageContainer = document.querySelector('[data-message]');

    if (messageContainer) {
        const messageText = messageContainer.textContent.trim();

        // メッセージが空でない場合、非表示設定を解除して表示する
        if (messageText !== "") {
            // style.display を 'none' から変更（CSSに合わせて調整可能。block, flex等）
            messageContainer.style.display = 'block';
            console.log('システムメッセージを表示しました:', messageText);

            // 5秒後にメッセージを非表示にする
            setTimeout(() => {
                messageContainer.style.display = 'none';
            }, 5000);
        }
    }
});