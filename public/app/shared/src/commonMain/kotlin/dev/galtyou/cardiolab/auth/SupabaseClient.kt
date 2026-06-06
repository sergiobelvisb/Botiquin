package dev.galtyou.cardiolab.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://harldwwpmzamjwhdpxcz.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imhhcmxkd3dwbXphbWp3aGRweGN6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAzMjIyOTksImV4cCI6MjA5NTg5ODI5OX0.AmZbxm9FYf_b5if8l1Pf8o1c7rk0ypvL-A7zTDcb1CU"
) {
    install(Auth)
}