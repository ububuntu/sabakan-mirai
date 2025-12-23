// テーマ変更関数
function changeTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
}

// ページ読み込み時にテーマを復元
window.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('theme') || 'blue'; // デフォルトはブルー
    document.documentElement.setAttribute('data-theme', savedTheme);

    // プルダウンの選択状態も復元
    const themeToggle = document.querySelector('.theme-toggle');
    if (themeToggle) {
        themeToggle.value = savedTheme;
    }
});