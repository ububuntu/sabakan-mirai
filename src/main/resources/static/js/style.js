// Cookie操作用のユーティリティ関数
const CookieManager = {
    // Cookieを設定
    set: function(name, value, days = 365) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        const expires = "expires=" + date.toUTCString();
        document.cookie = name + "=" + value + ";" + expires + ";path=/";
    },

    // Cookieを取得
    get: function(name) {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for(let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    // Cookieを削除
    delete: function(name) {
        document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    }
};

// テーマ変更関数
function changeTheme(theme) {
    // data-theme属性を設定
    document.documentElement.setAttribute('data-theme', theme);

    // Cookieに保存（1年間有効）
    CookieManager.set('theme', theme, 365);

    console.log('テーマを変更しました:', theme);
}

// ページ読み込み時にテーマを復元
window.addEventListener('DOMContentLoaded', () => {
    // Cookieからテーマを取得（デフォルトは'default'）
    const savedTheme = CookieManager.get('theme') || 'default';

    // テーマを適用
    document.documentElement.setAttribute('data-theme', savedTheme);

    // プルダウンの選択状態も復元
    const colorSelect = document.getElementById('color');
    if (colorSelect) {
        colorSelect.value = savedTheme;
    }

    console.log('保存されたテーマを読み込みました:', savedTheme);
});

// オプション: テーマをリセットする関数
function resetTheme() {
    CookieManager.delete('theme');
    document.documentElement.setAttribute('data-theme', 'default');
    const colorSelect = document.getElementById('color');
    if (colorSelect) {
        colorSelect.value = 'default';
    }
    console.log('テーマをリセットしました');
}