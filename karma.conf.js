module.exports = function (config) {
    config.set({
        browsers: ['MyHeadlessChrome'],
        // The directory where the output file lives
        basePath: 'target',
        // The file itself
        files: ['ci.js'],
        frameworks: ['cljs-test'],
        plugins: ['karma-cljs-test', 'karma-chrome-launcher', 'karma-junit-reporter'],
        colors: true,
        logLevel: config.LOG_INFO,
        client: {
            args: ["shadow.test.karma.init"],
            singleRun: true
        },
       customLaunchers: {
             MyHeadlessChrome: {
               base: 'ChromeHeadless',
               flags: [
                "--privileged",
                "--disable-gpu",
                "--start-maximized",
                "--disable-infobars",
                "--disable-extensions",
                "--disable-dev-shm-usage",
                "--no-sandbox",
                "--whitelisted-ips=''",
                "--disable-popup-blocking",
                "--chrome-frame",
                "--ignore-certificate-errors",
                "--disable-dev-shm-usage",
                "--disable-ipc-flooding-protection",
                "--disable-backgrounding-occluded-window",
                "--disable-breakpad",
                "--password-store=basic"]
             }
           }
    })
};
