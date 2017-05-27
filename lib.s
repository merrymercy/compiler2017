;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;; lib begin ;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

__int_format:
    db "%ld", 0

__intln_format:
    db "%ld", 10, 0

;========== IO ==========
ALIGN 16
__lib_printInt:
    sub rsp, 8

    mov rsi, rdi
    mov rdi, __int_format
    xor rax, rax
    call printf

    add rsp, 8
    ret

ALIGN 16
__lib_printlnInt:
    sub rsp, 8

    mov rsi, rdi
    mov rdi, __intln_format
    xor rax, rax
    call printf

    add rsp, 8
    ret

ALIGN 16
getInt:
    sub rsp, 8

    mov rsi, rsp
    mov rdi, __int_format
    xor eax, eax
    call scanf
    call getchar
    mov rax, [rsp]

    add rsp, 8
    ret


ALIGN   16
__lib_str_operator_ADD:
        push    r15
        push    r14
        mov     r15, rdi
        push    r13
        push    r12
        mov     r13, rsi
        push    rbp
        push    rbx
        sub     rsp, 8
        movsxd  rbx, dword [rdi-4H]
        movsxd  r12, dword [rsi-4H]
        lea     ebp, [rbx+r12]
        lea     edi, [rbp+1H]
        movsxd  rdi, edi
        add     rdi, 4
        call    malloc
        test    ebx, ebx
        mov     dword [rax], ebp
        mov     r14, rax
        lea     rbp, [rax+4H]
        jle     L_001
        lea     edx, [rbx-1H]
        mov     rsi, r15
        mov     rdi, rbp
        add     rdx, 1
        call    memcpy
L_001:  add     rbp, rbx
        test    r12d, r12d
        jle     L_002
        lea     edx, [r12-1H]
        lea     rdi, [r14+rbx+4H]
        mov     rsi, r13
        add     rdx, 1
        call    memcpy
L_002:  mov     byte [rbp+r12], 0
        mov     rax, rbp
        add     rsp, 8
        sub     rax, rbx
        pop     rbx
        pop     rbp
        pop     r12
        pop     r13
        pop     r14
        pop     r15
        ret

ALIGN   16
__lib_str_substring:
        push    r14
        push    r13
        push    r12
        push    rbp
        mov     r12, rdi
        push    rbx
        mov     ebx, edx
        mov     ebp, esi
        sub     ebx, esi
        lea     edi, [rbx+2H]
        lea     r13d, [rbx+1H]
        movsxd  rdi, edi
        add     rdi, 4
        call    malloc
        test    r13d, r13d
        mov     r14, rax
        mov     dword [rax], r13d
        lea     rcx, [rax+4H]
        jle     L_003
        mov     edx, ebx
        movsxd  rsi, ebp
        mov     rdi, rcx
        add     rdx, 1
        add     rsi, r12
        call    memcpy
        mov     rcx, rax
L_003:  movsxd  r13, r13d
        mov     rax, rcx
        pop     rbx
        mov     byte [r14+r13+4H], 0
        pop     rbp
        pop     r12
        pop     r13
        pop     r14
        ret
        nop

ALIGN   16
toString:
        push    rbx
        mov     ebx, edi
        mov     edi, 16
        call    malloc
        test    ebx, ebx
        mov     r9, rax
        lea     rdi, [rax+4H]
        js      L_007
        jne     L_010
        lea     rcx, [rax+5H]
        mov     byte [rax+4H], 48
        mov     rsi, rcx
L_004:  mov     rax, rcx
        mov     byte [rcx], 0
        sub     rax, rdi
        mov     dword [r9], eax
        lea     rax, [rcx-1H]
        cmp     rax, rsi
        jc      L_006

ALIGN   8
L_005:  movzx   edx, byte [rsi]
        movzx   ecx, byte [rax]
        add     rsi, 1
        sub     rax, 1
        mov     byte [rsi-1H], cl
        mov     byte [rax+1H], dl
        cmp     rsi, rax
        jbe     L_005
L_006:  mov     rax, rdi
        pop     rbx
        ret

ALIGN   8
L_007:  lea     rsi, [rax+5H]
        mov     byte [rax+4H], 45
        neg     ebx
L_008:  mov     rcx, rsi
        mov     r8d, 1717986919

ALIGN   8
L_009:  mov     eax, ebx
        add     rcx, 1
        imul    r8d
        mov     eax, ebx
        add     ebx, 48
        sar     eax, 31
        sar     edx, 2
        sub     edx, eax
        lea     eax, [rdx+rdx*4]
        add     eax, eax
        sub     ebx, eax
        test    edx, edx
        mov     byte [rcx-1H], bl
        mov     ebx, edx
        jnz     L_009
        jmp     L_004

L_010:  mov     rsi, rdi
        jmp     L_008

ALIGN 16
toString_hand:
    push rbx
    sub rsp, 128

    mov rdx, rdi
    mov rdi, rsp
    mov rsi, __int_format
    xor rax, rax
    call sprintf

    mov rdi, rsp
    call strlen

    mov rbx, rax ; length
    lea rdi, [rax + 5]
    call malloc
    mov dword [rax], ebx; store len

    lea rdi, [rax + 4]; 
    mov rsi, rsp
    call strcpy

    add rsp, 128
    pop rbx
    ret

ALIGN 16
getString:
    push rbx
    sub rsp, 128

    mov rdi, rsp
    call gets

    mov rdi, rsp
    call strlen

    mov rbx, rax ; length
    lea rdi, [rax + 5]
    call malloc

    mov dword [rax], ebx ; store length

    lea rdi, [rax + 4]
    mov rsi, rsp
    call strcpy

    add rsp, 128
    pop rbx
    ret

ALIGN 16
__lib_array_size:
    mov eax, dword [rdi -4]

    ret

ALIGN 16
__lib_str_operator_ADD_hand:
    push rbx
    push r12
    push r13

    mov r12, rdi ; save parameters
    mov r13, rsi

    mov ebx, dword [r12 - 4]
    add ebx, dword [r13 - 4] ; store length

    lea rdi, [rbx + 5]
    call malloc

    mov dword [rax], ebx
    lea rdi, [rax + 4]
    mov rsi, r12
    call strcpy

    mov rbx, rax ; return value

    mov edi, dword [r12 - 4]
    add rdi, rbx
    mov rsi, r13
    call strcpy

    mov rax, rbx

    pop r13
    pop r12
    pop rbx
    ret

ALIGN 16
__lib_str_substring_hand:
    push rbx
    push r12
    sub rsp, 8
    
    lea r12, [rdi + rsi]
    neg rsi
    lea rbx, [rdx + rsi + 1]; length

    lea rdi, [rbx + 5]
    call malloc

    mov dword[rax], ebx
    lea rdi, [rax + 4]
    lea rsi, [r12]
    mov rdx, rbx
    call strncpy

    add rsp, 8
    pop r12
    pop rbx
    ret

ALIGN 16
__lib_str_length:
    mov eax, dword [rdi -4]

    ret

ALIGN 16
__lib_str_parseInt:
    sub rsp, 8

    mov rsi, __int_format
    mov rdx, rsp
    xor rax, rax
    call sscanf

    mov eax, dword [rsp]
    add rsp, 8
    ret

ALIGN 16
__lib_str_ord:
    xor rax, rax
    mov al, byte [rdi + rsi]

    ret

ALIGN 16
__lib_str_operator_LT:
    sub rsp, 8

    call strcmp
    cmp rax, 0
    setl al
    movzx rax, al

    add rsp, 8
    ret

ALIGN 16
__lib_str_operator_EQ:
    sub rsp, 8

    call strcmp
    cmp rax, 0
    sete al
    movzx rax, al

    add rsp, 8
    ret

ALIGN 16
__lib_str_operator_NE:
    sub rsp, 8

    call strcmp
    cmp rax, 0
    setne al
    movzx rax, al

    add rsp, 8
    ret

ALIGN 16
__lib_str_operator_GT:
    sub rsp, 8

    call strcmp
    cmp rax, 0
    setg al
    movzx rax, al

    add rsp, 8
    ret

ALIGN 16
print:
    sub rsp, 8

    xor rax, rax
    call printf

    add rsp, 8
    ret

ALIGN 16
println:
    sub rsp, 8

    call puts

    add rsp, 8
    ret
