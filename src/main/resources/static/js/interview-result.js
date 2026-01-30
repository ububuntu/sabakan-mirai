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
        console.log('=== 面接結果ページ読み込み ===');
        loadAndDisplayResults().catch(function(error) {
            console.error('❌ loadAndDisplayResults実行エラー:', error);
            showErrorMessage('結果の読み込み中にエラーが発生しました: ' + error.message);
        });
    });

    /**
     * 結果を読み込んで表示
     */
    async function loadAndDisplayResults() {
        try {
            console.log('Step 1: localStorageチェック');

            // まずlocalStorageから取得を試みる
            const storedData = localStorage.getItem('interviewResult');

            if (storedData) {
                try {
                    const result = JSON.parse(storedData);
                    console.log('✅ localStorageから結果取得');
                    console.log(result);
                    displayResults(result);
                    return;
                } catch (error) {
                    console.error('❌ localStorage解析エラー:', error);
                }
            }

            console.log('Step 2: APIから取得');

            // localStorageになければAPIから取得
            await fetchResultFromAPI();

        } catch (error) {
            console.error('❌ loadAndDisplayResults内エラー:', error);
            throw error;
        }
    }

    /**
     * APIから結果を取得
     */
    async function fetchResultFromAPI() {
        try {
            console.log('📡 fetch開始: /interview/api/sessions/result');

            const response = await fetch('/interview/api/sessions/result');

            console.log('📡 fetch完了: status=' + response.status);

            if (!response.ok) {
                throw new Error('API応答エラー: ' + response.status + ' ' + response.statusText);
            }

            const data = await response.json();
            console.log('📡 JSON解析完了:', data);

            if (data.status === 'success' && data.data) {
                console.log('✅ API結果取得成功');
                console.log(data.data);
                displayResults(data.data);
            }else if(data.status === 'success' && !data.data){
                console.warn('⚠️ dataプロパティなし、テストレスポンスの可能性:', data);
                showErrorMessage('面接結果データがありません。APIの実装を確認してください。');
            } else {
                console.error('❌ API結果取得失敗:', data);
                showErrorMessage('面接結果が見つかりません。先に面接を完了してください。');
            }

        } catch (error) {
            console.error('❌ fetchResultFromAPI内エラー:', error);
            console.error('エラー詳細:', {
                name: error.name,
                message: error.message,
                stack: error.stack
            });
            showErrorMessage('結果の読み込みに失敗しました: ' + error.message);
        }
    }

    /**
     * 結果を画面に表示
     */
    function displayResults(result) {
        try {
            console.log('Step 3: 結果表示開始');

            const scores = result.scores || {};
            const comments = result.comments || {};

            console.log('=== 表示するデータ ===');
            console.log('スコア:', scores);
            console.log('コメント:', comments);

            // レーダーチャートを更新
            updateRadarChart(scores);

            // コメントを更新
            updateComments(comments);

            console.log('✅ 結果表示完了');

        } catch (error) {
            console.error('❌ displayResults内エラー:', error);
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

            console.log('📊 チャート更新:', {
                expression: expression,
                eyes: eyes,
                posture: posture,
                speechSpeed: speechSpeed
            });

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
                console.log('✅ チャート描画完了');
            } else {
                console.warn('⚠️ チャートパス要素が見つかりません');
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
                console.log('✅ 頂点更新完了');
            } else {
                console.warn('⚠️ 頂点要素が見つかりません: ' + circles.length + '個');
            }

        } catch (error) {
            console.error('❌ updateRadarChart内エラー:', error);
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
                    console.log('✅ ' + item.key + 'コメント更新');
                } else if (!element) {
                    console.warn('⚠️ 要素が見つかりません: ' + item.selector);
                } else if (!comments[item.key]) {
                    console.warn('⚠️ コメントが見つかりません: ' + item.key);
                }
            });

        } catch (error) {
            console.error('❌ updateComments内エラー:', error);
        }
    }

    /**
     * エラーメッセージを表示
     */
    function showErrorMessage(message) {
        message = message || '結果の読み込みに失敗しました。';

        console.error('💥 エラーメッセージ表示:', message);

        const notes = document.querySelectorAll('.note');
        notes.forEach(function(note) {
            note.textContent = message;
            note.style.color = '#dc3545';
        });
    }

})();