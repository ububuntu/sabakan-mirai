/**
 * 面接結果表示専用スクリプト
 * このファイルは interview-result.html でのみ使用
 */

(function() {
    'use strict';

    /**
     * ページ読み込み時に結果を表示
     */
    window.addEventListener('load', function() {
        loadAndDisplayResults().catch(function(error) {
            showErrorMessage('結果の読み込み中にエラーが発生しました: ' + error.message);
        });
    });

    /**
     * 結果を読み込んで表示
     */
    async function loadAndDisplayResults() {
        try {
            // まずlocalStorageから取得を試みる
            const storedData = localStorage.getItem('interviewResult');

            if (storedData) {
                try {
                    const result = JSON.parse(storedData);
                    displayResults(result);
                    return;
                } catch (error) {
                }
            }

            // localStorageになければAPIから取得
            await fetchResultFromAPI();

        } catch (error) {
            throw error;
        }
    }

    /**
     * APIから結果を取得
     */
    async function fetchResultFromAPI() {
        try {
            const response = await fetch('/interview/api/sessions/result');

            if (!response.ok) {
                throw new Error('API応答エラー: ' + response.status + ' ' + response.statusText);
            }

            const data = await response.json();

            if (data.status === 'success' && data.data) {
                displayResults(data.data);
            }else if(data.status === 'success' && !data.data){
                showErrorMessage('面接結果データがありません。APIの実装を確認してください。');
            } else {
                showErrorMessage('面接結果が見つかりません。先に面接を完了してください。');
            }

        } catch (error) {
            showErrorMessage('結果の読み込みに失敗しました: ' + error.message);
        }
    }

    /**
     * 結果を画面に表示
     */
    function displayResults(result) {
        try {
            const scores = result.scores || {};
            const comments = result.comments || {};

            // レーダーチャートを更新
            updateRadarChart(scores);

            // コメントを更新
            updateComments(comments);

        } catch (error) {
            showErrorMessage('結果の表示中にエラーが発生しました: ' + error.message);
        }
    }

    /**
     * レーダーチャートを更新
     */
    function updateRadarChart(scores) {
        try {
            const expression = parseInt(scores.expression) || 0;
            const eyes = parseInt(scores.eyes) || 0;
            const posture = parseInt(scores.posture) || 0;
            const speechSpeed = parseInt(scores.speechSpeed) || 0;

            // SVG座標計算（中心100, 最大半径100）
            const centerX = 100;
            const centerY = 100;
            const maxRadius = 100;

            // 各軸の座標
            const topY = centerY - (expression * maxRadius / 100);
            const rightX = centerX + (eyes * maxRadius / 100);
            const bottomY = centerY + (speechSpeed * maxRadius / 100);
            const leftX = centerX - (posture * maxRadius / 100);

            // パス作成
            const path = 'M ' + centerX + ' ' + topY +
                        ' L ' + rightX + ' ' + centerY +
                        ' L ' + centerX + ' ' + bottomY +
                        ' L ' + leftX + ' ' + centerY + ' Z';

            // チャート更新
            const chartPath = document.querySelector('.radar-chart-1 path[fill="#2589d030"]');
            if (chartPath) {
                chartPath.setAttribute('d', path);
            }

            // 頂点更新
            const circles = document.querySelectorAll('.radar-chart-1 g[fill="#2589d0"] circle');
            if (circles.length >= 4) {
                circles[0].setAttribute('cx', centerX);
                circles[0].setAttribute('cy', topY);
                circles[1].setAttribute('cx', rightX);
                circles[1].setAttribute('cy', centerY);
                circles[2].setAttribute('cx', centerX);
                circles[2].setAttribute('cy', bottomY);
                circles[3].setAttribute('cx', leftX);
                circles[3].setAttribute('cy', centerY);
            }

        } catch (error) {
        }
    }

    /**
     * コメントを更新
     */
    function updateComments(comments) {
        try {
            const commentMap = [
                { selector: '.menu-item:nth-child(2) .note', key: '表情' },
                { selector: '.menu-item:nth-child(3) .note', key: '視線' },
                { selector: '.menu-item:nth-child(4) .note', key: '姿勢' },
                { selector: '.menu-item:nth-child(5) .note', key: '発話速度' }
            ];

            commentMap.forEach(function(item) {
                const element = document.querySelector(item.selector);
                if (element && comments[item.key]) {
                    element.textContent = comments[item.key];
                }
            });

        } catch (error) {
        }
    }

    /**
     * エラーメッセージを表示
     */
    function showErrorMessage(message) {
        message = message || '結果の読み込みに失敗しました。';

        const notes = document.querySelectorAll('.note');
        notes.forEach(function(note) {
            note.textContent = message;
            note.style.color = '#dc3545';
        });
    }

})();