html, body {
  font-family: Arial, sans-serif;
  height: 100%;
  margin: 0px;
  padding: 0px;
}

#main {
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: 80px 1fr 40px;
  height: 100%;
}

#content {
  outline: solid 1px darkgrey;
  background: url('/images/graph-paper-50.png');
  padding: 8px;
  grid-column: 1 / span 1;
  grid-row: 2 / span 1;
  margin: 20px;
}

.text-only {
  display:none;
}

.highlighted {
  background: yellow;
  outline: solid red 2px;
  font-weight: bold;
}

#top-menu {
  position: fixed;
  top: 0px;
  margin: 0px;
  width: 100%;
  height: 80px;
  border-bottom: solid 4px ${color1};
  background: ${color3};
}

#top-menu ul {
  list-style-type: none;
  margin: 0px;
  margin-top: 12px;
}

#top-menu ul li {
  display: inline-block;
  margin: 0px;
  margin-right: 8px;
  padding: 4px;
  border: solid 1px #d8d8d8;
  border-radius: 4px;
  box-shadow: 2px 2px 0px 0px rgba(115,115,115,1);
}

#top-menu ul li:hover {
  background: white;
  border-color: #ccc;
}

#top-menu ul li a {
  text-decoration: none;
  color: #222;
  height: 48px;
  line-height: 48px;
  display: inline-block;
}

#top-menu ul li.selected {
  color: #422;
  background: white;
}

#top-menu-home {
  background: url('/images/home-48.png') left center no-repeat;
  padding-left: 56px;
}

#top-menu-status {
  background: url('/images/status-48.png') left center no-repeat;
  padding-left: 56px;
}

#top-menu-experiments {
  background: url('/images/erlenmeyer-48.png') left center no-repeat;
  padding-left: 56px;
}

#top-menu-assistant {
  background: url('/images/assistant-48.png') left center no-repeat;
  padding-left: 32px;
}

#top-menu-plots {
  background: url('/images/graph-48.png') left center no-repeat;
  padding-left: 56px;
}

#top-menu-tables {
  background: url('/images/table-48.png') left center no-repeat;
  padding-left: 56px;
}

#top-menu-find {
  background: url('/images/find-48.png') left center no-repeat;
  padding-left: 56px;
}

#top-menu-macros {
  background: url('/images/tulip-48.png') left center no-repeat;
  padding-left: 56px;
}

#top-menu-help {
  background: url('/images/help-48.png') left center no-repeat;
  padding-left: 56px;
}

#footer {
  position: fixed;
  height: 40px;
  line-height: 40px;
  border-top: solid 4px ${color1};
  bottom: 0px;
  width: 100%;
  font-size: 80%;
  display: grid;
  grid-template-rows: 1fr;
  grid-template-columns: 104px 1fr;
  background: ${color3} url('/images/lif-24.png') center right 8px no-repeat;
}

#logo-container {
  grid-column: 1 / span 1;
  grid-row: 1 / span 1;
}

#labpal-logo {
  background: url('/images/labpal-32.png') top left no-repeat;
  width: 98px;
  height: 32px;
  margin-top: 4px;
  display: block;
}

#labpal-logo span {
  display: none;
}

#footer-text {
  grid-column: 2 / span 1;
  grid-row: 1 / span 1;
}

table th {
  text-align: left;
}

table.exp-table th {
  font-weight: bold;
  background: ${color3};
}

.top-td {
  background: ${color3};
}

table.exp-table tr:hover td {
  background-color: rgba(224, 224, 236, 0.5);
}

table.exp-table td.exp-chk {
  background: url('/images/erlenmeyer-16.png') right center no-repeat;
  padding-right: 20px;
}

table.exp-table td {
  padding: 4px;
}

.status-icon {
  display: inline-block;
  height: 24px;
  min-width: 24px;
  margin-right: 12px;
}

.status-icon.status-done {
  background: url('/images/status-done-24.png') left center no-repeat;
}

.status-icon.status-warning {
  background: url('/images/status-warning-24.png') left center no-repeat;
}

.status-icon.status-exclamation {
  background: url('/images/status-exclamation-24.png') left center no-repeat;
}

.status-icon.status-running {
  background: url('/images/status-running-24.png') left center no-repeat;
}

.status-icon.status-ready {
  background: url('/images/status-ok-24.png') left center no-repeat;
}

.status-icon.status-failed {
  background: url('/images/status-fail-24.png') left center no-repeat;
}

.status-icon.status-killed {
  background: url('/images/status-timeout-24.png') left center no-repeat;
}

.status-icon.status-unknown {
  background: url('/images/status-unknown-24.png') left center no-repeat;
}

.status-icon.status-queued {
  background: url('/images/status-queued-24.png') left center no-repeat;
}

.status-icon.status-prereq {
  background: url('/images/status-prereq-24.png') left center no-repeat;
}

.status-icon > span {
	display: none;
}

.pulldown {
  padding-left: 24px;
  background: url('/images/expanded-24.png') top left no-repeat;
}

.pulldown.closed {
  background: url('/images/collapsed-24.png') top left no-repeat;
}

.tables .table-icon {
  width: 24px;
  background: url('/images/table-24.png') top center no-repeat;
  padding-right: 8pt;
}

.tables a, .exp-table a {
  text-decoration: none;
  color: ${color2};
}

.tables a:hover, .exp-table a:hover {
  color: ${color1};
}

.btn {
  border: solid 1px darkgrey;
  border-radius: 4px;
  height: 32px;
  line-height: 20px;
  background: ${color4};
  padding: 4px;
  display: inline-block;
  text-decoration: none;
  color: #222;
  box-shadow: 2px 2px 0px 0px rgba(115,115,115,1);
}

.btn:hover, .btn-24:hover {
  background-color: #e8e8e8;
}

.btn-24 {
  border: solid 1px darkgrey;
  border-radius: 4px;
  height: 24px;
  line-height: 24px;
  background: ${color4};
  padding: 4px;
  display: inline-block;
  text-decoration: none;
  color: #222;
  box-shadow: 2px 2px 0px 0px rgba(115,115,115,1);
}

.btn-download {
  background: ${color4} url('/images/download-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-upload {
  background: ${color4} url('/images/upload-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-start {
  background: ${color4} url('/images/running-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-stop {
  background: ${color4} url('/images/abort-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-queue {
  background: ${color4} url('/images/queue-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-unqueue {
  background: ${color4} url('/images/remove-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-delete {
  background: ${color4} url('/images/remove-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-reset {
  background: ${color4} url('/images/reset-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-compute-claims {
  background: ${color4} url('/images/reset-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-filter {
  background: ${color4} url('/images/find-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-clean {
  background: ${color4} url('/images/clean-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.btn-edit {
  background: ${color4} url('/images/pencil-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

#btn-find {
  background: ${color4} url('/images/find-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

#btn-report-results {
  background: ${color4} url('/images/wifi-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}


.btn-picture {
  background: ${color4} url('/images/image-24.png') 4px 4px no-repeat;
  padding-left: 40px;
}

.description {
  margin-bottom: 10pt;
}

.status-table {
  margin-top: 10pt;
  margin-bottom: 10pt;
}

.status-table th {
  background: ${color3};
}

.status-table td {
  background-color: rgba(224, 224, 236, 0.5);
}

.id-cell {
  font-weight: bold;
  font-style: italic;
  color: ${color1};
}

#content a {
  color: ${color2};
  text-decoration: none;
}

#content a:hover {
  color: ${color1};
  text-decoration: underline;
}

.message {
  padding-left: 40px;
  border: solid 1px darkgrey;
  border-radius: 4px;
  min-height: 48px;
  display: table-cell;
  vertical-align: middle;
  margin-bottom: 12pt;
}

.message span {
  margin: auto;
  display: block;
  margin: 6px;
}

.message.info {
  background: white url('/images/info-32.png') left center no-repeat;
}

.message.error {
  background: #fcc url('/images/error-32.png') left center no-repeat;
}

.progress-bar {
  border: solid 1px darkgrey;
  height: 16px;
  width: auto;
  list-style-type: none;
  margin: 0;
  padding: 0;
  margin-right: 12pt;
  background: white;
  display: inline-block;
}

.progress-bar li, .progress-bar > span {
  float: left;
  display: inline-block;
  height: 16px;
  margin: 0;
  padding: 0;
}

.progress-bar .done {
  background-color: green;
}

.progress-bar .done:hover {
  background-color: lightgreen;
}

.progress-bar .warning {
  background-color: orange;
}

.progress-bar .warning:hover {
  background-color: yellow;
}

.progress-bar .queued {
  background-color: gray;
}

.progress-bar .queued:hover {
  background-color: lightgray;
}

.progress-bar .failed {
  background-color: red;
}

.progress-bar .failed:hover {
  background-color: #f88;
}

.progress-bar .other {
  background-color: white;
}

.running-exps {
  padding-left: 0px;
}

.running-exps li {
  display: inline-block;
  list-style-type: none;
  background: url('/images/erlenmeyer-16.png') left no-repeat;
  padding-left: 22px;
  margin-right: 10px;
  border-radius: 3px;
}

.running-exps li:hover {
  background-color: #eee;
  background-opacity: 0.5;
}

#top-menu ul li.small-screen {
	background-position-x: center;
	width: 56px;
}

#top-menu ul li.small-screen a {
	background-position-x: center;
	padding-left: 10px;
	width: 48px;
}

#top-menu ul li.small-screen .menu-text {
	display: none;
}

.plot {
  width: 340px;
  height: 280px;
  float: left;
  margin: 20px;
  padding: 0px;
  list-style-type: none;
}

.plot img {
  width: 320px;
  height: 240px;
  border: solid 1px darkgrey;
  opacity: 0.67;
}

.plot img:hover {
  border: solid 1px blue;
  opacity: 1;
}

.plot ul, ul.plot-buttons {
  margin-left: 0;
  padding-left: 0;
}

.plot ul li, ul.plot-buttons li {
  list-style-type: none;
  display: inline-block;
  margin-right: 10px;
}

.btn-pdf {
  background: ${color4} url('/images/icon-pdf-24.png') 4px 4px no-repeat;
  padding-left: 32px;
}

.btn-gp {
  background: ${color4} url('/images/graph-24.png') 4px 4px no-repeat;
  padding-left: 32px;
}

.btn-table {
  background: ${color4} url('/images/table-24.png') 4px 4px no-repeat;
  padding-left: 32px;
}

.btn-csv {
  background: ${color4} url('/images/csv-24.png') 4px 4px no-repeat;
  padding-left: 32px;
}

.btn-tex {
  background: ${color4} url('/images/tex-24.png') 4px 4px no-repeat;
  padding-left: 32px;
}

.btn-html {
  background: ${color4} url('/images/html-24.png') 4px 4px no-repeat;
  padding-left: 32px;
}

.btn-all-plots {
  background: ${color4} url('/images/pages-24.png') 4px 4px no-repeat;
  padding-left: 28px;
}

.btn-all-plots-latex {
  background: ${color4} url('/images/tex-24.png') 4px 4px no-repeat;
  padding-left: 28px;
}

.line-align {
  display: flex;
  align-items: center;
}

.line-align > span {
  margin-right: 10px;
}

#status-line {
  display: flex;
  align-items: center;
}

#status-line span.status-icon {
  opacity: 0.2;
  margin-right: 0px;
  transform: scale(0.8);
  background-position: top left;
  height: 23px;
}

#status-line span.status-icon.active {
  opacity: 1;
}

#status-line span.status-icon.current {
  opacity: 1;
  transform: none;
  border: solid 3px darkgreen;
  border-radius: 3px;
}

#status-line span.status-icon.status-running.current {
  opacity: 1;
  transform: none;
  animation: blink-animation 1s steps(5, start) infinite;
}

@keyframes blink-animation {
        to {
          visibility: hidden;
        }
      }

#status-line .dash {
  border: solid 2px darkgreen;
  width: 16pt;
  height: 0px;
  opacity: 0.2;
}

#status-line .dash.active {
  opacity: 1;
}

#status-line .status-caption {
  margin-left: 16pt;
}

.json-table th {
  text-align: left;
  background: ${color3};
  margin: 2px;
  padding: 2px;
  border-radius: 2px;
}

.json-table td {
  margin: 2px;
  padding: 2px;
}