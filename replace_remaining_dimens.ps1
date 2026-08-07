$replacements = @{
    "@dimen/radius_sm" = "@dimen/_8sdp"
    "@dimen/radius_md" = "@dimen/_12sdp"
    "@dimen/radius_lg" = "@dimen/_20sdp"
    "@dimen/height_month_nav" = "@dimen/_36sdp"
    "@dimen/icon_size_nav" = "@dimen/_24sdp"
    "@dimen/text_small" = "@dimen/_12ssp"
    "@dimen/text_body" = "@dimen/_14ssp"
    "@dimen/text_body_large" = "@dimen/_16ssp"
    "@dimen/text_display" = "@dimen/_36ssp"
}

$files = @(
    "d:\VIMAL PLAYSTORE 2026\app\src\main\res\drawable\bg_input_field.xml",
    "d:\VIMAL PLAYSTORE 2026\app\src\main\res\layout\item_category_row.xml",
    "d:\VIMAL PLAYSTORE 2026\app\src\main\res\values\styles.xml",
    "d:\VIMAL PLAYSTORE 2026\app\src\main\res\values\themes.xml"
)

foreach ($file in $files) {
    $content = Get-Content $file -Raw
    foreach ($key in $replacements.Keys) {
        $content = $content -replace $key, $replacements[$key]
    }
    Set-Content -Path $file -Value $content
}
